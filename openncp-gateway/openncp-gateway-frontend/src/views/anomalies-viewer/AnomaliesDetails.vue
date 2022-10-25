<template>
  <v-container fluid>
    <div>
      <v-breadcrumbs :items="items">
        <template v-slot:divider>
          <v-icon>mdi-forward</v-icon>
        </template>
      </v-breadcrumbs>
    </div>
    <v-tabs v-model="tab">
      <v-tab href="#tab-event-identification">Anomaly Identification</v-tab>
      <!--v-tab href="#tab-active-participants">Active Participants</v-tab>
      <v-tab href="#tab-audit-source-identification"
        >Audit Source Identification</v-tab
      >
      <v-tab href="#tab-participant-object-identifications"
        >Participant Object Identifications</v-tab
      >
      <v-tab href="#tab-xml-message">View XML</v-tab-->
    </v-tabs>
    <div v-if="anomaly">
      <v-tabs-items v-model="tab">
        <v-tab-item value="tab-event-identification">
          <v-container fluid>
            <v-row>
              <v-col>
                <v-text-field
                  label="Anomaly Type"
                  outlined
                  :value="
                    anomaly.type
                  "
                  disabled
                  hide-details="auto"
                />
              </v-col>
              <v-col>
                <v-text-field
                  label="Anomaly Detection Date Time"
                  outlined
                  :value="
                    anomaly.eventDate | date('long')
                  "
                  disabled
                  hide-details="auto"
                />
              </v-col>
            </v-row>
            <v-row>
              <v-col>
                <v-text-field
                  label="Anomaly Begin Event Date Time"
                  outlined
                  :value="
                    anomaly.eventStartDate | date('long')
                  "
                  disabled
                  hide-details="auto"
                />
              </v-col>
              <v-col>
                <v-text-field
                  label="Anomaly End Event Date Time"
                  outlined
                  :value="
                    anomaly.eventEndDate | date('long')
                  "
                  disabled
                  hide-details="auto"
                />
              </v-col>
            </v-row>
            <v-row>
              <v-col>
                <v-textarea height="200" width="350"
                  label="Anomaly Description"
                  :value="
                    anomaly.description
                  "
                  outlined
                  disabled
                  hide-details="auto"
                />
              </v-col>
            </v-row>
          </v-container>
        </v-tab-item>
      </v-tabs-items>
    </div>
  </v-container>
</template>

<script>
import axios from 'axios'

export default {
  props: ['id'],
  data () {
    return {
      tab: 'tab-event-identification',
      loading: false,
      anomaly: null
    }
  },
  computed: {
    items () {
      const p = this.$router.currentRoute.path.split('/')
      p.pop()
      const a = this.$router.resolve({ name: 'anomalies-details' })
      console.log(a.href)
      return [
        {
          text: 'Anomaly Viewer',
          disabled: false,
          to: { name: 'anomalies' }
        },
        {
          text: 'Anomaly messages list',
          disabled: false,
          to: { name: 'anomalies' },
          exact: true
        },
        {
          text: this.id,
          disabled: true
        }
      ]
    }
  },
  mounted () {
    this.loading = true
    axios
      .get(process.env.VUE_APP_SERVER_URL + `/api/anomaly/anomalies/${this.id}`)
      .then((response) => {
        console.log(response)
        this.anomaly = response.data
        this.loading = false
      })
  },
  methods: {
    back () {
      this.$router.go(-1)
    }
  }
}
</script>

<style scoped>
pre.xml {
  font-size: 0.75em;
  overflow-x: auto;
}
</style>
