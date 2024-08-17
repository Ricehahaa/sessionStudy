<script setup>


</script>

<template>
  <div style="width: 100vw;height: 100vh;overflow: hidden;display: flex">
    <div style="flex: 1">
      <el-image style="width: 100%;height: 100%;"fit="cover" src="https://ts1.cn.mm.bing.net/th/id/R-C.ff1759e0bb7393b0dce1681203c76fbd?rik=jAv05j0pDtEgTA&riu=http%3a%2f%2fn.sinaimg.cn%2fsinacn20120%2f311%2fw951h960%2f20190831%2f3a52-icxmqsw2693437.jpg&ehk=siDhCHl62zTfEYCUSF3kXzbqmHhcVobhkeeTv17wmRA%3d&risl=&pid=ImgRaw&r=0"/>
    </div>
    <div style="width: 400px;background-color: white">
      <router-view v-slot="{ Component }">
        <transition name="el-fade-in-linear" mode="out-in">
          <component :is="Component" style="height: 100%"/>
        </transition>
      </router-view>

    </div>
  </div>
</template>

<style scoped>

</style>