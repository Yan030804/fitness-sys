import { request } from '../utils/request'
import { getCurrentUserDetail } from './user'
import { getWorkoutRecordList } from './workoutRecord'
import { getDietRecordList } from './dietRecord'
import { getPlanList, getPlanItems, completePlanItem } from './plan'
import { getMyAppointmentList, cancelAppointment } from './appointment'
import { getLatestRecommendations, generateRecommendations } from './recommendation'
import { chatAi } from './ai'

export {
  getCurrentUserDetail,
  getWorkoutRecordList,
  getDietRecordList,
  getPlanList,
  getPlanItems,
  completePlanItem,
  getMyAppointmentList,
  cancelAppointment,
  getLatestRecommendations,
  generateRecommendations,
  chatAi
}
