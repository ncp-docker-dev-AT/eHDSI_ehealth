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
      <v-tab href="#tab-event-identification">Event Identification</v-tab>
      <v-tab href="#tab-active-participants">Active Participants</v-tab>
      <v-tab href="#tab-audit-source-identification"
        >Audit Source Identification</v-tab
      >
      <v-tab href="#tab-participant-object-identifications"
        >Participant Object Identifications</v-tab
      >
      <v-tab href="#tab-xml-message">View XML</v-tab>
    </v-tabs>
    <div v-if="message">
      <v-tabs-items v-model="tab">
        <v-tab-item value="tab-event-identification">
          <v-container fluid>
            <v-row>
              <v-col>
                <v-text-field
                  label="Event Action Code"
                  outlined
                  :value="
                    message.auditMessage.eventIdentification.eventActionCode
                  "
                  disabled
                  hide-details="auto"
                />
              </v-col>
              <v-col>
                <v-text-field
                  label="Event Date Time"
                  outlined
                  :value="
                    message.auditMessage.eventIdentification.eventDateTime
                  "
                  disabled
                  hide-details="auto"
                />
              </v-col>
              <v-col>
                <v-text-field
                  label="Event Outcome Indicator"
                  outlined
                  :value="
                    message.auditMessage.eventIdentification
                      .eventOutcomeIndicator
                  "
                  disabled
                  hide-details="auto"
                />
              </v-col>
            </v-row>
            <v-row>
              <v-col>
                <v-text-field
                  label="Event ID Code"
                  outlined
                  :value="message.auditMessage.eventIdentification.eventID.code"
                  disabled
                  hide-details="auto"
                />
              </v-col>
              <v-col>
                <v-text-field
                  label="Display Name"
                  outlined
                  :value="
                    message.auditMessage.eventIdentification.eventID.displayName
                  "
                  disabled
                  hide-details="auto"
                />
              </v-col>
              <v-col>
                <v-text-field
                  label="Code System"
                  outlined
                  :value="
                    message.auditMessage.eventIdentification.eventID.codeSystem
                  "
                  disabled
                  hide-details="auto"
                />
              </v-col>
            </v-row>
          </v-container>
          <v-container
            fluid
            v-for="(eventTypeCode, i) in message.auditMessage
              .eventIdentification.eventTypeCode"
            :key="`event-type-code-${i}`"
          >
            <v-row>
              <v-col>Event Type Code #{{ i }}</v-col>
            </v-row>
            <v-row>
              <v-col>
                <v-text-field
                  label="Event Type Code"
                  outlined
                  :value="eventTypeCode.code"
                  disabled
                  hide-details="auto"
                />
              </v-col>
              <v-col>
                <v-text-field
                  label="Display Name"
                  outlined
                  :value="eventTypeCode.displayName"
                  disabled
                  hide-details="auto"
                />
              </v-col>
              <v-col>
                <v-text-field
                  label="Code System"
                  outlined
                  :value="eventTypeCode.codeSystem"
                  disabled
                  hide-details="auto"
                />
              </v-col>
            </v-row>
          </v-container>
        </v-tab-item>
        <v-tab-item value="tab-active-participants">
          <v-container
            fluid
            v-for="(activeParticipant, i) in message.auditMessage
              .activeParticipant"
            :key="`active-participant-${i}`"
          >
            <v-row>
              <v-col>Active Participant #{{ i }}</v-col>
            </v-row>
            <v-row>
              <v-col>
                <v-text-field
                  label="User ID"
                  outlined
                  :value="activeParticipant.userID"
                  disabled
                  hide-details="auto"
                />
              </v-col>
              <v-col>
                <v-text-field
                  label="Alternative User ID"
                  outlined
                  :value="activeParticipant.alternativeUserID"
                  disabled
                  hide-details="auto"
                />
              </v-col>
              <v-col>
                <v-text-field
                  label="User Is Requestor"
                  outlined
                  :value="activeParticipant.userIsRequestor"
                  disabled
                  hide-details="auto"
                />
              </v-col>
            </v-row>
            <v-row>
              <v-col>
                <v-text-field
                  label="Network Access Point ID"
                  outlined
                  :value="activeParticipant.networkAccessPointID"
                  disabled
                  hide-details="auto"
                />
              </v-col>
              <v-col>
                <v-text-field
                  label="Network Access Point Type Code"
                  outlined
                  :value="activeParticipant.networkAccessPointTypeCode"
                  disabled
                  hide-details="auto"
                />
              </v-col>
              <v-col />
            </v-row>
            <v-row
              v-for="(roleIDCode, j) in activeParticipant.roleIDCode"
              :key="`role-id-code-${j}`"
            >
              <v-col>
                <v-text-field
                  label="Role ID Code"
                  outlined
                  :value="roleIDCode.code"
                  disabled
                  hide-details="auto"
                />
              </v-col>
              <v-col>
                <v-text-field
                  label="Display Name"
                  outlined
                  :value="roleIDCode.displayName"
                  disabled
                  hide-details="auto"
                />
              </v-col>
              <v-col>
                <v-text-field
                  label="Code System"
                  outlined
                  :value="roleIDCode.codeSystem"
                  disabled
                  hide-details="auto"
                />
              </v-col>
            </v-row>
          </v-container>
        </v-tab-item>
        <v-tab-item value="tab-audit-source-identification">
          <v-container fluid>
            <v-row
              v-for="(auditSourceIdentification, i) in message.auditMessage
                .auditSourceIdentification"
              :key="`audit-source-${i}`"
            >
              <v-col cols="4">
                <v-text-field
                  label="Audit SourceID"
                  outlined
                  :value="auditSourceIdentification.auditSourceID"
                  disabled
                  hide-details="auto"
                />
              </v-col>
            </v-row>
          </v-container>
        </v-tab-item>
        <v-tab-item value="tab-participant-object-identifications">
          <h1>Active Participants</h1>
        </v-tab-item>
        <v-tab-item value="tab-xml-message">
          <v-container fluid>
            <v-card>
              <pre class="xml">{{ message.xmlMessage }}</pre>
            </v-card>
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
      message: null
    }
  },
  computed: {
    items () {
      const p = this.$router.currentRoute.path.split('/')
      p.pop()
      const a = this.$router.resolve({ name: 'audit-messages' })
      console.log(a.href)
      return [
        {
          text: 'ATNA Viewer',
          disabled: true,
          href: 'breadcrumbs_dashboard'
        },
        {
          text: 'Audit messages list',
          disabled: false,
          to: { name: 'audits' },
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
      .get(process.env.VUE_APP_SERVER_URL + `/api/atna/messages/${this.id}`)
      .then((response) => {
        this.message = response.data
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
