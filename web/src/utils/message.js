// 全局消息提示工具
// 用于在非组件环境中（如 axios 拦截器）显示消息

let messageInstance = null

export const setMessageInstance = (instance) => {
  messageInstance = instance
}

export const message = {
  success: (msg) => {
    if (messageInstance) {
      messageInstance.success(msg)
    } else {
      console.log('[Success]', msg)
    }
  },
  error: (msg) => {
    if (messageInstance) {
      messageInstance.error(msg)
    } else {
      console.error('[Error]', msg)
    }
  },
  warning: (msg) => {
    if (messageInstance) {
      messageInstance.warning(msg)
    } else {
      console.warn('[Warning]', msg)
    }
  },
  info: (msg) => {
    if (messageInstance) {
      messageInstance.info(msg)
    } else {
      console.info('[Info]', msg)
    }
  }
}
