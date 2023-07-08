import { defineStore } from "pinia";
import { request } from "~/utils/request";

export const useUserStore = defineStore('user', {
    state: () => {
        return {
            identity: '',
            username: '',
            name: '',
            type: '',
            idn: '',
            phone: '',
            mileage_points: 0,
            ali_balance: 0,
            wechat_balance: 0
        }
    },
    getters: {
        getUserName() {

        }
    },
    actions: {
        fetch() {
            request({
                url: '/user',
                method: 'GET'
            }).then((res) => {
                this.identity = res.data.data.identity;
                this.username = res.data.data.username;
                this.name = res.data.data.name;
                this.type = res.data.data.type;
                this.idn = res.data.data.idn;
                this.phone = res.data.data.phone;
                this.mileage_points = res.data.data.mileage_points;
                this.ali_balance = res.data.data.ali_balance;
                this.wechat_balance = res.data.data.wechat_balance;
            }).catch((err) => {
                console.log(err)
            })
        }
    }

})