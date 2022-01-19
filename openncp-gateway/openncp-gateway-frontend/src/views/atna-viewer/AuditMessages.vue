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
      <v-card-title>
        <v-spacer></v-spacer>
        <v-text-field
          v-model="search"
          append-icon="mdi-magnify"
          label="Search"
          single-line
          hide-details
        ></v-text-field>
      </v-card-title>
      <v-card-text>
        <v-data-table
          :headers="headers"
          :items="messages"
          :options.sync="options"
          :server-items-length="totalMessages"
          :loading="loading"
        >
          <template v-slot:[`item.actions`]="{ item }">
            <v-btn
              fab
              x-small
              color="indigo"
              :to="{ name: 'audit-details', params: { id: item.id } }"
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
      headers: [
        { text: '#', value: 'id' },
        { text: 'Event Action Code', value: 'eventActionCode' },
        { text: 'Event ID Code', value: 'eventId' },
        { text: 'Event Type Codes', value: 'eventTypes' },
        { text: 'Event Date Time', value: 'eventDateTime' },
        { text: 'Event Outcome Indicator', value: 'eventOutcomeIndicator' },
        { value: 'actions', sortable: false }
      ],
      messages: [],
      options: {},
      search: '',
      loading: true,
      items: [
        {
          text: 'ATNA Viewer',
          disabled: true
        },
        {
          text: 'Audit messages list',
          disabled: true
        }
      ]
    }
  },
  mounted () {
    this.getDataFromApi()
  },
  watch: {
    options: {
      handler () {
        this.getDataFromApi()
      },
      deep: true,
      itemsPerPage: 10
    }
  },
  methods: {
    getDataFromApi () {
      this.loading = true
      this.apiCall().then((data) => {
        this.messages = data.content
        this.totalMessages = data.totalElements
        this.options.page = this.number
        this.loading = false
      })
    },
    apiCall () {
      return axios.get(process.env.VUE_APP_SERVER_URL + '/api/atna/messages', {
        params: { pageable: { pageNumber: page, pageSize: 10 } }
      })
    }
  }
}
</script>
