<template>
  <v-container fluid>
    <div>
      <v-breadcrumbs :items="items">
        <template v-slot:divider>
          <v-icon>mdi-forward</v-icon>
        </template>
      </v-breadcrumbs>
    </div>
    <v-card>
      <v-card-text>
        <v-data-table
          :headers="headers"
          :items="anomalies"
          :disable-items-per-page="true"
          :loading="loading"
        >
          <template v-slot:[`item.anomalyDateTime`]="{ item }">
            <span>{{ new Date(item.anomalyDateTime).toLocaleString() }}</span>
          </template>
          <template v-slot:[`item.beginEventDateTime`]="{ item }">
            <span>{{ new Date(item.beginEventDateTime).toLocaleString() }}</span>
          </template>
          <template v-slot:[`item.endEventDateTime`]="{ item }">
            <span>{{ new Date(item.endEventDateTime).toLocaleString() }}</span>
          </template>
          <template v-slot:[`item.actions`]="{ item }">
            <v-btn
              fab
              x-small
              color="indigo"
              :to="{ name: 'anomalies-details', params: { id: item.id } }"
            >
              <v-icon>mdi-eye</v-icon>
            </v-btn>
          </template>
        </v-data-table>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script>
import axios from 'axios'

export default {
  data () {
    return {
      dialog: false,
      headers: [
        { text: 'Id', value: 'id' },
        { text: 'Description', value: 'anomalyDescription' },
        { text: 'Type', value: 'anomalyType' },
        { text: 'Detect Date', value: 'anomalyDateTime', dataType: 'Date' },
        { text: 'Begin Date', value: 'beginEventDateTime', dataType: 'Date' },
        { text: 'End Date', value: 'endEventDateTime', dataType: 'Date' },
        { value: 'actions', sortable: false }
      ],
      anomalies: [],
      loading: false,
      items: [
        {
          text: 'Anomalies Viewer',
          disabled: true
        },
        {
          text: 'Anomalies list',
          disabled: true
        }
      ]
    }
  },
  mounted () {
    this.getDataFromApi()
  },
  methods: {
    getDataFromApi () {
      if (!this.loading) {
        this.loading = true
        axios
          .get(process.env.VUE_APP_SERVER_URL + '/api/anomaly/anomalies')
          .then((response) => {
            console.log(response.data)
            this.anomalies = response.data
            this.loading = false
          })
      }
    }
  }
}
</script>
