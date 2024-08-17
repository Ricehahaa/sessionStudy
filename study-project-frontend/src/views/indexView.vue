<script setup>
import {ElMessage} from "element-plus";
import router from "@/router/index.js";
import {get} from "@/net/index.js"
import {useStore} from "@/stores/counter.js";

const store = useStore()

const logout = () => {
  get('/api/auth/logout',(message) => {
    ElMessage.success(message)
    store.auth.user = null
    router.push('/')
  })
}
</script>

<template>
  <div>
    进入到学习平台
  </div>
  <div>
    <el-button @click='logout()' type="danger" plain>退出登录</el-button>
  </div>
</template>

<style scoped>


</style>