import Vue from 'vue'
import Moment from 'moment'
import App from './App.vue'
import router from './router'
import store from './store'
import vuetify from './plugins/vuetify'

Vue.config.productionTip = false

Vue.mixin({
  methods: {
    validatePasswordRuleComplexity: function (data) {
      return /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,30}$/.test(data)
    }
  }
})

Vue.filter('date', function (value, formatType = 'long') {
  if (formatType === 'short') {
    return Moment(value).format('LLLL')
  } else if (formatType === 'long') {
    return Moment(value).format('DD MMMM YYYY, h:mm:ss a')
  } else {
    return 'Bad Date Format'
  }
})

new Vue({
  router,
  store,
  vuetify,
  render: h => h(App)
}).$mount('#app')
