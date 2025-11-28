<template>
  <n-config-provider :theme-overrides="themeOverrides">
    <n-message-provider>
      <n-dialog-provider>
        <AppContent />
      </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
import { defineComponent, h } from 'vue'
import { NConfigProvider, NMessageProvider, NDialogProvider, useMessage } from 'naive-ui'
import { setMessageInstance } from '@/utils/message'
import { RouterView } from 'vue-router'

const themeOverrides = {
  common: {
    primaryColor: '#18a058',
    primaryColorHover: '#36ad6a',
    primaryColorPressed: '#0c7a43'
  }
}

// 创建一个内部组件来获取 message 实例
const AppContent = defineComponent({
  setup() {
    const message = useMessage()
    setMessageInstance(message)
    
    return () => h(RouterView)
  }
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial,
    'Noto Sans', sans-serif;
}
</style>
