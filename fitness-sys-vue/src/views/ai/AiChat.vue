<template>
  <div class="ai-chat-page">
    <!-- <el-card class="intro-card" shadow="never">
      <!-- <div class="page-header">
        <div>
          <div class="page-title">智能问答</div>
          <div class="page-subtitle">可向私人健身助手咨询训练、饮食、减脂、增肌等问题</div>
        </div>
      </div> -->
    <!-- </el-card> -->

    <el-card class="chat-card" shadow="never">
      <template #header>
        <div class="chat-header">
          <div>
            <div class="chat-title">私人健身助手 AI</div>
            <div class="chat-subtitle">基于你的问题提供健身与饮食建议</div>
          </div>
          <el-button plain @click="handleClearSession" :disabled="isSending">清空会话</el-button>
        </div>
      </template>

      <div class="quick-question-wrap">
        <div class="section-label">快捷提问</div>
        <div class="quick-question-list">
          <el-button
            v-for="question in quickQuestions"
            :key="question"
            class="quick-btn"
            plain
            @click="handleQuickQuestion(question)"
          >
            {{ question }}
          </el-button>
        </div>
      </div>

      <div ref="messageListRef" class="message-list">
        <div v-if="messages.length === 1" class="welcome-empty">
          <div class="welcome-icon">AI</div>
          <div class="welcome-title">开始和私人健身助手对话</div>
          <div class="welcome-text">你可以先点击上方快捷提问，也可以直接输入自己的问题。</div>
        </div>

        <div
          v-for="message in messages"
          :key="message.id"
          class="message-row"
          :class="message.role === 'user' ? 'is-user' : 'is-ai'"
        >
          <div class="message-meta">
            <span class="role-label">{{ message.role === 'user' ? '我' : 'AI 助手' }}</span>
            <span class="time-label">{{ formatTime(message.createdAt) }}</span>
          </div>

          <div class="message-bubble" :class="message.role === 'user' ? 'user-bubble' : 'ai-bubble'">
            <template v-if="message.role === 'user'">
              <p class="message-text">{{ message.content }}</p>
            </template>
            <template v-else>
              <div
                v-if="message.content"
                class="ai-message-markdown"
                v-html="renderMarkdown(message.content)"
              ></div>
              <p v-else-if="isSending" class="message-text">正在思考中...</p>
            </template>

            <div v-if="message.references?.length" class="reference-box">
              <div class="reference-title">提示信息</div>
              <div
                v-for="(item, index) in message.references"
                :key="`${message.id}-${index}`"
                class="reference-item"
              >
                {{ item }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="composer-wrap">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="4"
          resize="none"
          placeholder="请输入你想咨询的问题，例如：我想减脂，晚餐应该怎么吃？"
          @keydown="handleInputKeydown"
        />
        <div class="composer-footer">
          <span class="composer-tip">Enter 发送，Shift + Enter 换行</span>
          <el-button type="primary" :loading="isSending" :disabled="!canSend" @click="handleSend">
            发送
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, nextTick, ref } from 'vue'
import { ElMessage } from 'element-plus'
import MarkdownIt from 'markdown-it'
import DOMPurify from 'dompurify'
import { chatAi, streamChatAi } from '../../api/ai'

const md = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: true,
  typographer: true
})

const normalizeAiDisplayText = (text) => {
  if (!text) return ''
  return text
    .replace(/(^|\n)\s*([1-9]\d*)[\.、]\s+/g, '$1$2）')
}

const renderMarkdown = (content) => {
  if (!content) return ''
  const normalized = normalizeAiDisplayText(content)
  const html = md.render(normalized)
  return DOMPurify.sanitize(html)
}

const quickQuestions = [
  '我想减脂，怎么安排训练？',
  '增肌期怎么吃？',
  '新手一周训练几次比较合适？',
  '跑步和力量训练怎么搭配？'
]

let messageId = 1
const createMessage = (role, content = '', extra = {}) => ({
  id: `msg-${messageId++}`,
  role,
  content,
  createdAt: new Date().toISOString(),
  references: [],
  ...extra
})

const inputMessage = ref('')
const isSending = ref(false)
const messageListRef = ref(null)
const messages = ref([
  createMessage(
    'assistant',
    '您好，我是您的私人健身助手，您可以问我减脂、增肌、训练安排、饮食建议等问题。'
  )
])

const canSend = computed(() => {
  return inputMessage.value.trim().length > 0 && !isSending.value
})

const scrollToBottom = async () => {
  await nextTick()
  const el = messageListRef.value
  if (!el) return
  el.scrollTop = el.scrollHeight
}

const formatTime = (value) => {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  const pad = (num) => String(num).padStart(2, '0')
  return `${pad(date.getHours())}:${pad(date.getMinutes())}`
}

const APPOINTMENT_REFERENCE_TEXT =
  '如需预约，请提供相关信息，格式如下：预约类型：健身评估、预约日期：2026-05-01，预约时间9:00'

const normalizeReferenceText = (text) => {
  const content = String(text || '').trim()
  if (!content) return ''

  if (content.includes('如需预约')) {
    return APPOINTMENT_REFERENCE_TEXT
  }

  return content
}

const normalizeReferences = (payload) => {
  if (!Array.isArray(payload)) return []
  return [...new Set(payload.filter(Boolean).map((item) => normalizeReferenceText(item)).filter(Boolean))]
}

const extractDonePayload = (payload) => {
  if (!payload || typeof payload !== 'object') {
    return {
      answer: typeof payload === 'string' ? payload : '',
      references: [],
      suggestionType: ''
    }
  }

  const source = payload.data && typeof payload.data === 'object' ? payload.data : payload
  return {
    answer: source.answer || source.content || source.text || '',
    references: normalizeReferences(source.references),
    suggestionType: source.suggestionType || ''
  }
}

const mergeStreamText = (currentText, incomingText) => {
  const current = String(currentText || '')
  const incoming = String(incomingText || '')

  if (!incoming) return current
  if (!current) return incoming
  if (incoming === current) return current

  // 有些后端返回的是“累计内容”，直接用更长的一份即可
  if (incoming.startsWith(current)) return incoming
  if (current.startsWith(incoming)) return current

  // 处理首尾重叠，避免同一句尾部被多次拼接
  const maxOverlap = Math.min(current.length, incoming.length)
  for (let overlap = maxOverlap; overlap > 0; overlap -= 1) {
    if (current.slice(-overlap) === incoming.slice(0, overlap)) {
      return current + incoming.slice(overlap)
    }
  }

  return current + incoming
}

const sendWithFallback = async (question, aiMessage) => {
  try {
    let finished = false

    await streamChatAi({
      message: question,
      onChunk: async (chunk) => {
        if (!chunk) return
        aiMessage.content = mergeStreamText(aiMessage.content, chunk)
        await scrollToBottom()
      },
      onDone: async (payload) => {
        const donePayload = extractDonePayload(payload)
        if (donePayload.answer && donePayload.answer.trim()) {
          aiMessage.content = donePayload.answer
        }
        aiMessage.references = donePayload.references
        if (donePayload.suggestionType) {
          aiMessage.suggestionType = donePayload.suggestionType
        }
        finished = true
        await scrollToBottom()
      },
      onError: async (messageText) => {
        throw new Error(messageText || '流式问答失败')
      }
    })

    if (!finished && !aiMessage.content) {
      throw new Error('流式响应未返回有效内容')
    }
  } catch (streamError) {
    const res = await chatAi({ message: question })
    const payload = extractDonePayload(res)
    aiMessage.content = payload.answer || '抱歉，暂时没有生成有效回复。'
    aiMessage.references = payload.references
  }
}

const handleSendText = async (question) => {
  const content = question.trim()
  if (!content || isSending.value) return

  const userMessage = createMessage('user', content)
  const aiMessage = createMessage('assistant', '')

  messages.value.push(userMessage)
  messages.value.push(aiMessage)
  inputMessage.value = ''
  isSending.value = true
  await scrollToBottom()

  try {
    await sendWithFallback(content, aiMessage)
  } catch (error) {
    aiMessage.content = '抱歉，本次问答请求失败，请稍后重试。'
    aiMessage.references = []
    ElMessage.error(error?.message || '智能问答请求失败')
  } finally {
    isSending.value = false
    await scrollToBottom()
  }
}

const handleSend = async () => {
  await handleSendText(inputMessage.value)
}

const handleQuickQuestion = (question) => {
  inputMessage.value = question
}

const handleClearSession = () => {
  if (isSending.value) return
  messages.value = [
    createMessage(
      'assistant',
      '您好，我是您的私人健身助手，您可以问我减脂、增肌、训练安排、饮食建议等问题。'
    )
  ]
  inputMessage.value = ''
  scrollToBottom()
}

const handleInputKeydown = (event) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    handleSend()
  }
}
</script>

<style scoped>
.ai-chat-page {
  min-height: calc(100vh - 120px);
}

.intro-card,
.chat-card {
  border-radius: 14px;
}

.intro-card {
  margin-bottom: 20px;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
}

.page-subtitle {
  margin-top: 6px;
  color: #909399;
  font-size: 13px;
}

.chat-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.chat-title {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}

.chat-subtitle {
  margin-top: 6px;
  color: #909399;
  font-size: 13px;
}

.quick-question-wrap {
  margin-bottom: 18px;
  padding: 16px 18px;
  background: #f8fafc;
  border: 1px solid #eef2f7;
  border-radius: 12px;
}

.section-label {
  margin-bottom: 12px;
  color: #606266;
  font-size: 13px;
  font-weight: 600;
}

.quick-question-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.quick-btn {
  border-radius: 999px;
  background: #fff;
}

.message-list {
  height: 520px;
  padding: 6px 4px 12px;
  overflow-y: auto;
  border: 1px solid #eef2f7;
  border-radius: 12px;
  background: linear-gradient(180deg, #fafcff 0%, #f8fafc 100%);
}

.welcome-empty {
  display: flex;
  min-height: 160px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30px 20px 18px;
  text-align: center;
  color: #64748b;
}

.welcome-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  margin-bottom: 14px;
  border-radius: 50%;
  background: #dbeafe;
  color: #2563eb;
  font-weight: 700;
}

.welcome-title {
  margin-bottom: 8px;
  color: #1f2937;
  font-size: 16px;
  font-weight: 700;
}

.welcome-text {
  max-width: 420px;
  line-height: 1.7;
  font-size: 13px;
}

.message-row {
  display: flex;
  flex-direction: column;
  margin: 16px;
}

.message-row.is-user {
  align-items: flex-end;
}

.message-row.is-ai {
  align-items: flex-start;
}

.message-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  color: #94a3b8;
  font-size: 12px;
}

.message-bubble {
  max-width: min(78%, 760px);
  padding: 14px 16px;
  border-radius: 14px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 4px 10px rgba(15, 23, 42, 0.04);
}

.ai-bubble {
  background: #fff;
}

.user-bubble {
  background: #eff6ff;
  border-color: #bfdbfe;
}

.message-text {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  color: #303133;
  font-size: 14px;
  line-height: 1.9;
}

.reference-box {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #e5e7eb;
}

.reference-title {
  margin-bottom: 8px;
  color: #909399;
  font-size: 12px;
  font-weight: 600;
}

.reference-item {
  color: #606266;
  font-size: 12px;
  line-height: 1.7;
}

.composer-wrap {
  margin-top: 18px;
  padding: 16px 18px;
  border: 1px solid #eef2f7;
  border-radius: 12px;
  background: #fff;
}

.composer-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 12px;
}

.composer-tip {
  color: #909399;
  font-size: 12px;
}

.ai-message-markdown {
  white-space: normal;
  line-height: 1.8;
  font-size: 14px;
  color: #333;
  word-break: break-word;
}

.ai-message-markdown :deep(p) {
  margin: 6px 0;
}

.ai-message-markdown :deep(h1),
.ai-message-markdown :deep(h2),
.ai-message-markdown :deep(h3) {
  margin: 12px 0 8px;
  font-weight: 600;
  line-height: 1.5;
}

.ai-message-markdown :deep(h1) {
  font-size: 18px;
}

.ai-message-markdown :deep(h2) {
  font-size: 16px;
}

.ai-message-markdown :deep(h3) {
  font-size: 15px;
}

.ai-message-markdown :deep(strong) {
  font-weight: 600;
}

.ai-message-markdown :deep(ol),
.ai-message-markdown :deep(ul) {
  margin: 8px 0;
  padding-left: 24px;
}

.ai-message-markdown :deep(li) {
  margin: 4px 0;
  line-height: 1.8;
  padding-left: 2px;
}

.ai-message-markdown :deep(li > p) {
  margin: 0;
}

.ai-message-markdown :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 10px 0;
  font-size: 13px;
}

.ai-message-markdown :deep(th),
.ai-message-markdown :deep(td) {
  border: 1px solid #e5e7eb;
  padding: 6px 8px;
  text-align: left;
}

.ai-message-markdown :deep(th) {
  background: #f7f8fa;
  font-weight: 600;
}

.ai-message-markdown :deep(code) {
  background: #f5f5f5;
  padding: 2px 4px;
  border-radius: 4px;
  font-size: 13px;
}

.ai-message-markdown :deep(pre) {
  background: #f5f5f5;
  padding: 10px;
  border-radius: 8px;
  overflow-x: auto;
}

.ai-message-markdown :deep(pre code) {
  background: transparent;
  padding: 0;
}

@media (max-width: 992px) {
  .message-bubble {
    max-width: 88%;
  }
}

@media (max-width: 768px) {
  .chat-header,
  .page-header,
  .composer-footer {
    flex-direction: column;
    align-items: flex-start;
  }

  .message-list {
    height: 460px;
  }

  .message-bubble {
    max-width: 100%;
  }
}
</style>
