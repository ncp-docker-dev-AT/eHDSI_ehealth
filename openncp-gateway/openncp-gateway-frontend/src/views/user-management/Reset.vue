<template>
  <v-content>
    <v-container class="fill-height">
      <v-row class="justify-center">
        <v-card width="400">
          <v-card-text>
            <form ref="form">
              <h3>Reset Password</h3>
              <br />

              <v-text-field
                v-model="password"
                type="password"
                :rules="passwordRulesComplexity"
                label="New Password"
                required
              ></v-text-field>
              <v-text-field
                v-model="confirmPwd"
                type="password"
                :rules="passwordRuleMatch"
                label="Confirm New Password"
                required
              ></v-text-field>
            </form>
          </v-card-text>
          <v-card-actions class="justify-center">
            <v-btn @click="handleSubmit" color="indigo"> Submit </v-btn>
          </v-card-actions>
        </v-card>
      </v-row>
    </v-container>
  </v-content>
</template>

<script>
import axios from 'axios'
export default {
  name: 'Reset',
  data () {
    return {
      password: '',
      confirmPwd: '',

      passwordRulesComplexity: [
        (v) => !!v || 'Password is required',
        (v) => v.length <= 30 || '30 Characters max',
        (v) =>
          this.validatePasswordRuleComplexity(v) ||
          'Password must be at least 8 characters with at least one uppercase letter, one lowercase letter, one number and one special character and no white spaces'
      ],
      passwordRuleMatch: [
        (v) => {
          return this.password === this.confirmPwd || "Passwords don't match"
        }
      ]
    }
  },
  methods: {
    async handleSubmit () {
      await axios
        .post(
          process.env.VUE_APP_SERVER_URL + '/api/user/reset-password/finish',
          {
            password: this.password,
            token: this.$route.query.key
          }
        )
        .then(() => {
          this.$router.push('/login')
        })
        .catch((err) => {
          this.error('An error occurred : ' + err.response.data, err)
        })
    }
  }
}
</script>
