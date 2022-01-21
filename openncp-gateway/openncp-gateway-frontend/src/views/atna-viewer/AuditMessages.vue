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
        <v-card-title>
          <v-spacer></v-spacer>
          <v-text-field
            v-model="searchEventId"
            append-icon="mdi-magnify"
            label="Search Event ID"
            single-line
            hide-details
          ></v-text-field>
          <v-menu
            v-model="searchStartDateMenu"
            :close-on-content-click="false"
            max-width="290"
          >
            <template v-slot:activator="{ on, attrs }">
              <v-text-field
                clearable
                readonly
                label="Event Date"
                v-bind="attrs"
                v-on="on"
                @click:clear="searchEventStartDate = null"
              ></v-text-field>
            </template>
            <v-spacer></v-spacer>
            <v-date-picker
              v-model="searchEventStartDate"
              @change="searchStartDateMenu = false"
            ></v-date-picker>
          </v-menu>
          <v-menu
            v-model="searchEndDateMenu"
            :close-on-content-click="false"
            max-width="290"
          >
            <template v-slot:activator="{ on, attrs }">
              <v-text-field
                clearable
                readonly
                label="Event Outcome"
                v-bind="attrs"
                v-on="on"
                @click:clear="searchEventEndDate = null"
              ></v-text-field>
            </template>
            <v-spacer></v-spacer>
            <v-date-picker
              v-model="searchEventEndDate"
              @change="searchEndDateMenu = false"
            ></v-date-picker>
          </v-menu>
          <v-spacer></v-spacer>
          <v-btn block @click="getDataFromApi"> Search </v-btn>
        </v-card-title>
        <v-data-table
          :headers="headers"
          :items="messages"
          :disable-items-per-page="true"
          :footer-props="{
            'items-per-page-options': [10]
          }"
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
        { text: '#', value: 'id', sortable: false },
        {
          text: 'Event Action Code',
          value: 'eventActionCode',
          sortable: false
        },
        { text: 'Event ID Code', value: 'eventId', sortable: false },
        { text: 'Event Type Codes', value: 'eventTypes', sortable: false },
        { text: 'Event Date Time', value: 'eventDateTime', sortable: false },
        {
          text: 'Event Outcome Indicator',
          value: 'eventOutcomeIndicator',
          sortable: false
        },
        { value: 'actions', sortable: false }
      ],
      messages: [],
      totalMessages: 0,
      options: { page: 1, itemsPerPage: 10 },
      loading: true,
      searchEventId: '',
      searchEventStartDate: '',
      searchEventEndDate: '',
      searchStartDateMenu: false,
      searchEndDateMenu: false,
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
      page: 1,
      itemsPerPage: 10,
      deep: true
    }
  },
  computed: {
    // convertStartDate () {
    //   return this.searchEventStartDate
    //     ? this.convertDate(this.searchEventStartDate)
    //     : ''
    // },
    // convertEndDate () {
    //   return this.searchEventEndDate
    //     ? this.convertDate(this.searchEventEndDate)
    //     : ''
    // }
  },
  methods: {
    convertDate (d) {
      if (d) {
        console.log('d', d, typeof d)
        const dd = new Date(d)
        console.log('dd', dd)
        const ddd = new Date(dd.getTime())
        console.log('ddd', ddd)
        return ddd
      }
      return ''
    },
    getDataFromApi () {
      this.loading = true
      this.apiCall().then((data) => {
        this.messages = data.data.content
        this.totalMessages = data.data.totalElements
        this.options.page = data.data.number + 1
        this.loading = false
      })
    },
    apiCall () {
      return axios.get(process.env.VUE_APP_SERVER_URL + '/api/atna/messages', {
        params: {
          pageNumber: this.options.page - 1,
          size: this.options.itemsPerPage,
          searchEventId: this.searchEventId,
          searchEventStartDate: this.convertDate(this.searchEventStartDate),
          searchEventEndDate: this.convertDate(this.searchEventEndDate)
        }
      })
    }
  }
}
</script>
