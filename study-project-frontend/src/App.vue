<script setup>
import {get} from "@/net"
import {ElMessage} from "element-plus";
import {useStore} from "@/stores/counter.js";
import router from "@/router/index.js";

const store = useStore()
if(store.auth.user == null){
  get("/api/user/me",(message) => {
    store.auth.user = message
    ElMessage.success('已登录')
    router.push('/index')
  },() => {
    store.auth.user = null
  })
}

</script>

<template>
  <router-view></router-view>
</template>


<style scoped>

</style>
