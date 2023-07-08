<script setup lang="ts">

import {computed, h, onMounted, reactive, ref} from "vue";
import { request } from "~/utils/request";
import { parseDate } from "~/utils/date";
import { Right } from "@element-plus/icons-vue";
import { useStationsStore } from "~/stores/stations";
import { useRouter } from "vue-router";
import { OrderDetailData } from "~/utils/interfaces";
import {ElMessage, ElNotification} from "element-plus";

let orders = reactive({
  data: [] as OrderDetailData[]
})

const router = useRouter()
const stations = useStationsStore()

let dialog = ref(false)
let dialog_refund = ref(false)
let id = ref()

let orderDetail = reactive<{ data: OrderDetailData }>({
    data: {
        id: 0,
        train_id: 0,
        seat: '',
        status: '',
        created_at: '',
        start_station_id: 0,
        end_station_id: 0,
        departure_time: '',
        arrival_time: '',
        //TODO
        price: 0,
        balance: 0,
        payment_strategy: '',
        bonus_points: 0,
        consumption_points: 0,
        left_points: 0,
        use_points: false,
    },
})
//TODO
const getOrderDetail = (id: number) => {
    request({
        url: `/order/${id}`,
        method: 'GET',
    }).then(res => {
        orderDetail.data = res.data.data
        console.log(orderDetail.data)
    }).catch(err => {
        console.log(err)
        if (err.response?.data.code == 100003) {
            router.push('/login')
        }
        ElNotification({
            offset: 70,
            title: 'getOrder错误',
            message: h('i', { style: 'color: teal' }, err.response?.data.msg),
        })
    })
}
//TODO
const refund = (id: number) => {
    request({
        url: `/order/${id}`,
        method: 'PATCH',
        data: {
            status: '已退款'
        }
    }).then((res) => {
        ElNotification({
            offset: 70,
            title: '退款成功',
            message: h('success', { style: 'color: teal' }, res.data.msg),
        })
        getOrderDetail(id)
        console.log(orderDetail.data.left_points)
        getOrders()
        console.log(res)
    }).catch((error) => {
        if (error.response?.data.code == 100003) {
            router.push('/login')
        }
        ElNotification({
            offset: 70,
            title: '退款失败',
            message: h('error', { style: 'color: teal' }, error.response?.data.msg),
        })
        console.log(error)
    })
}

const getOrders = () => {
  request({
    url: '/order',
    method: 'GET'
  }).then((res) => {
    orders.data = res.data.data
  }).catch((error) => {
    if (error.response?.data.code == 100003) {
      router.push('/login')
    }
    console.log(error)
  })
}

const getTrainName = (id: number) => {
  request({
    url: `/train/${id}`,
    method: 'GET'
  }).then((res) => {
    return res.data.name
  }).catch((err) => {
    console.log(err)
  }
  )
}

onMounted(() => {
  getOrders()
  stations.fetch()
})

</script>

<template>
  <el-card v-for="order in orders.data " style="margin-bottom: 1vh" shadow="hover">
    <div style="display: flex; flex-direction: column">

      <div style="display: flex; justify-content: space-between;">
        <div>
          <el-text size="large" tag="b" type="primary">
            订单号:&nbsp;&nbsp;
          </el-text>
          <el-text size="large" tag="b">
            {{ order.id }}
          </el-text>
        </div>
        <div>
          <el-text size="large" tag="b" type="primary">
            创建日期:&nbsp;&nbsp;
          </el-text>
          <el-text size="large" tag="b">
            {{ parseDate(order.created_at) }}
          </el-text>
        </div>
      </div>

      <div>
        <el-text size="large" tag="b" type="primary">
          订单状态:&nbsp;&nbsp;
        </el-text>
        <el-text size="large" tag="b">
          {{ order.status }}
        </el-text>
      </div>

      <el-row class="el-row">
        <el-col :span="24" style="display: flex; justify-content: center; align-items: center">
          <el-text type="primary" size="large" tag="b">
            {{ getTrainName(order.train_id) }}
          </el-text>
        </el-col>
      </el-row>

      <el-row justify="center" class="el-row">
        <el-col :span="11" style="display: flex; justify-content: right; align-items: center">
          <el-text>
            {{ stations.idToName[order.start_station_id] }}
          </el-text>
        </el-col>
        <el-col :span="2" style="display: flex; justify-content: center; align-items: center">
          <el-icon size="15">
            <Right />
          </el-icon>
        </el-col>
        <el-col :span="11" style="display: flex; justify-content: left; align-items: center;">
          <el-text style="text-align: center">
            {{ stations.idToName[order.end_station_id] }}
          </el-text>
        </el-col>
      </el-row>

      <el-row justify="center">
        <el-col :span="11" style="display: flex; justify-content: right; align-items: center">
          <el-text>
            {{ parseDate(order.departure_time) }}
          </el-text>
        </el-col>
        <el-col :span="2">
        </el-col>
        <el-col :span="11" style="display: flex; justify-content: left; align-items: center">
          <el-text>
            {{ parseDate(order.arrival_time) }}
          </el-text>
        </el-col>
      </el-row>

      <div style="display: flex; justify-content: space-between; margin-top: 20px;">
          <div>
              <div v-if="order.status === '等待支付'">
                  <el-button type="primary" @click="dialog = true; id = order.id">
                      支付
                  </el-button>
              </div>
              <div v-else-if="order.status === '已退款'">
                  您已退款
              </div>
              <div v-else-if="order.status === '已完成'">
                  <el-button type="danger" @click="refund(order.id ?? -1); dialog_refund = true">
                      退款
                  </el-button>
              </div>
          </div>
          <el-button type="primary" @click="dialog = true; id = order.id">
              查看详情
          </el-button>
      </div>

    </div>
  </el-card>

  <el-dialog destroy-on-close v-model="dialog" title="订单详情" width="50%" @close="getOrders">
    <OrderDetail :id="id" />
  </el-dialog>

  <el-dialog destroy-on-close v-model="dialog_refund" title="退款详情" width="50%" @close="getOrders">
      <div style="margin-left: 20px">
        <div>
          <el-text size="large" tag="b" type="primary">
              价格:
          </el-text>
          <el-text size="large" tag="b">
              {{ orderDetail.data.price }}
          </el-text>
        </div>
        <div>
            <el-text size="large" tag="b" type="primary">
                您的{{ orderDetail.data.payment_strategy }}余额为:
            </el-text>
            <el-text size="large" tag="b">
                {{ orderDetail.data.balance }}
            </el-text>
        </div>
        <div>
            <el-text size="large" tag="b" type="primary">
                奖励积分:
            </el-text>
            <el-text size="large" tag="b">
                {{ orderDetail.data.bonus_points }}
            </el-text>
        </div>
        <div>
            <el-text size="large" tag="b" type="primary">
                消耗积分:
            </el-text>
            <el-text size="large" tag="b">
                {{ orderDetail.data.consumption_points }}
            </el-text>
        </div>
        <div>
            <el-text size="large" tag="b" type="primary">
                您的剩余里程积分为:
            </el-text>
            <el-text size="large" tag="b">
                {{ orderDetail.data.left_points }}
            </el-text>
        </div>
      </div>

      <template #footer>
          <div class="dialog-footer" style="text-align: center">
            <el-button type="primary" size="large" @click="dialog_refund = false" @close="getOrders">确认</el-button>
          </div>
      </template>
  </el-dialog>
</template>

<style scoped></style>