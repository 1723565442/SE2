<script lang="ts" setup>
import {useUserStore} from "~/stores/user";

const user = useUserStore()

</script>

<template>
  <el-container>
    <el-header style="position: fixed; width: 100%; z-index: 999">
      <MenuComponent pageIndex="/" />
    </el-header>
    <el-main style="margin-top: 10vh">
      <el-row justify="center" style="display: flex; align-content: center;height: 85vh;">
        <el-col :span="24" style="display: flex; justify-content: center; align-items: center; flex-direction: column;">
            <Title style="margin-bottom: 20px" />
        </el-col>
        <el-col :span="12" style="display: flex; align-items: center" v-if="user.identity === '用户' || user.identity === '管理员'">
          <el-card class="search-ticket-card" shadow="hover" header="车票查询">
            <SearchTicketForm :inline="false" @formUpdated="() => { $router.push('/search#query'); }"></SearchTicketForm>
          </el-card>
        </el-col>
        <div v-if="user.identity === ''">
          <el-col :span="24" style="display: flex; justify-content: center; align-items: center; flex-direction: column;">
              团队：满江红
          </el-col>
            <div style="margin-top: 20px;">
                -------------------------------------
            </div>
          <el-col :span="24" style="display: flex; justify-content: center; align-items: center; flex-direction: column;">
            <div>
              <div>
                <el-text size="large" tag="b" type="primary">
                    组长:
                </el-text>
                <el-text size="large" tag="b">
                    211250243聂尔聪
                </el-text>
              </div>
              <div>
                <el-text size="large" tag="b" type="primary">
                    组员:
                </el-text>
                <el-text size="large" tag="b">
                    211250242 罗晗
                </el-text>
              </div>
              <div>
                <el-text size="large" tag="b" type="primary">
                    组员:
                </el-text>
                <el-text size="large" tag="b">
                    211250183 曲廷锌
                </el-text>
              </div>
              <div>
                <el-text size="large" tag="b" type="primary">
                    组员:
                </el-text>
                <el-text size="large" tag="b">
                    211250225 王渤元
                </el-text>
              </div>
            </div>
          </el-col>
        </div>
      </el-row>
    </el-main>
  </el-container>
</template>

<style scoped>
.search-ticket-card {
  width: 500px;
  height: 300px;
  margin: 0 auto 20vh;
}
</style>

