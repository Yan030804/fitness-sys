import { getToken } from '../utils/auth'
import { request } from '../utils/request'

const getApiBaseUrl = () => {
  return import.meta.env.DEV ? '' : import.meta.env.VITE_API_BASE_URL || ''
}

export function chatAi(data) {
  return request({
    url: '/api/v1/ai/chat',
    method: 'post',
    data
  })
}

const parseSseData = (raw) => {
  if (!raw) return ''

  try {
    return JSON.parse(raw)
  } catch (error) {
    return raw
  }
}

const getChunkText = (payload) => {
  if (typeof payload === 'string') return payload
  if (!payload || typeof payload !== 'object') return ''

  return (
    payload.chunk ??
    payload.text ??
    payload.content ??
    payload.answerPart ??
    payload.answer ??
    payload.data ??
    ''
  )
}

export async function streamChatAi({ message, onChunk, onDone, onError }) {
  const token = getToken()
  const response = await fetch(`${getApiBaseUrl()}/api/v1/ai/chat/stream`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'text/event-stream',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    body: JSON.stringify({ message })
  })

  if (!response.ok || !response.body) {
    const errorText = await response.text().catch(() => '')
    throw new Error(errorText || `请求失败：${response.status}`)
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''

  while (true) {
    const { value, done } = await reader.read()
    if (done) break

    buffer += decoder.decode(value, { stream: true })
    const events = buffer.split('\n\n')
    buffer = events.pop() || ''

    for (const block of events) {
      const lines = block
        .split('\n')
        .map((line) => line.trim())
        .filter(Boolean)

      if (!lines.length) continue

      let eventName = 'message'
      const dataLines = []

      lines.forEach((line) => {
        if (line.startsWith('event:')) {
          eventName = line.slice(6).trim()
        } else if (line.startsWith('data:')) {
          dataLines.push(line.slice(5).trim())
        }
      })

      const payload = parseSseData(dataLines.join('\n'))

      if (eventName === 'chunk') {
        onChunk?.(getChunkText(payload), payload)
      } else if (eventName === 'done') {
        onDone?.(payload)
      } else if (eventName === 'error') {
        const messageText =
          typeof payload === 'string'
            ? payload
            : payload?.message || payload?.error || '流式响应失败'
        onError?.(messageText, payload)
      }
    }
  }
}
