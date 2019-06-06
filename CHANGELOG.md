# Change Log

All notable changes to the <a href="https://ec.europa.eu/cefdigital/wiki/x/8CEZAg" rel="nofollow">CEF EHDSI project</a> will be documented in this file.<br/> 
This project adheres to <a href="http://semver.org/" rel="nofollow">Semantic Versioning</a>.

## [3.0.0] - 04-06-2019
### Added
- Wave 2: CDA Implementation Guide & CDA DisplayTool [EHNCP-1724] [EHNCP-1726] [EHNCP-1727].
- ISM XSD doesn't allow more than 1 textField element [EHNCP-1751].
- ISM XSD doesn't accept friendlyName attribute on elements other than textField [EHNCP-1750].
- CDA Display Tool - Clinical Manifestation is not translated [EHNCP-1732].
- Root CA Certificates from eDelivery SMP have to be imported automatically in OpenNCP truststore [EHNCP-1570].

### Changed
- Alignment of CDA Extended definition with Art Decor [EHNCP-1633].
- OpenSAML framework into OpenNCP Portal doesn't start correctly in some situation [EHNCP-1762].
- Audit Service - FailedLogHandler doesn't work properly [EHNCP-1765].
- OpenNCP Commons Components - Dynamic Discovery [EHNCP-1764].
- OpenNCP Web Portal - unexpected exceptions when multiple users are logged in [EHNCP-1244].
- TSAM Exporter does not recognize correctly Environment variable [EHNCP-1722].
- CDA Display Tool - Minor improvements [EHNCP-1725] [EHNCP-1743] [EHNCP-1744] [EHNCP-1757].
- First-Time Configuration Utility doesn't recognize properties table [EHNCP-1696].
- Minor code refactoring and BugFix on OpenNCP components [EHNCP-1698].
  
### Deprecated
- N/A

### Removed
- Remove the Database Logging (Logback) [EHNCP-1759].                    

## [3.0.0.RC4] - 30-04-2019
### Added
- Full support of CDA with elements and namespace prefixes [EHNCP-1716].
- SMP-Editor - XSD validation of the International Search Mask [EHNCP-1742].
- eHDSI Gazelle validation (asynchronous call, threading mechanism) and improvement of the evidences generated (format).
- SMP audit message adding correct NetworkAccessPointID [EHNCP-1723].
- Improvement of the data received from the Portal (trim of PAtient Demographics).
- ATC code added to coded_element_list_epSOS2.xml configuration file.
- Exception handling enhancement: eD submission (XDR) [EHNCP-1377] and XCA List and Retrieve at NCP-A [EHNCP-1425].

### Changed
- CDA Document processing are producing xmlns empty attributes [EHNCP-1714].
- eHDSI Gazelle missing evidences [EHNCP-1718] [EHNCP-1719].
- XCPD request and response, adding precision for creation date.
- Adapt Home Community ID for Spain.
- OpenNCP Commons Components - minor bug fix.
- SAML Assertions fix (Web Portal) and version alignment among the project [EHNCP-1729].
- OpenNCP National Connector Mock implementation - minor bug fix.
- CDA Display Tool enhancement.
- Audit message content fix after an error has been raised [EHNCP-1166].
- Logging appender misconfiguration fixed [EHNCP-1676].
  
### Deprecated
- eSense obligation for Consent transactions.
- Patient Consent handling out of eHDSI scope (creation, revocation).
- Eclipse Oomph setup removed.

### Removed
- N/A

## [3.0.0.RC3] - 10-02-2019
### Added
- N/A

### Changed
- N/A
  
### Deprecated
- N/A

### Removed
- Dependency openws.jar has been removed from the component Evidence Emitter to prevent classloading issue [EHNCP-1697]

## [3.0.0.RC2] - 06-02-2019
### Added
- eHDSI Gazelle platform updated: all the validators are now labelled according the Wave & Specification version. 

### Changed
- Identity HCP Assertions validity has been extended to 4 hours instead of 2 hours (conformance to specification).
- Removing Personal Data (Patient ID) from the NRO evidences file produced during the Dispense (XDR).
- CDA DisplayTool improvements and alignment to eHDSI Specification (Change Proposal 09).
- Server Monitoring (Javamelody) and Running Mode (PAT messages) tools have been fixed (minor issue).
- Bug fix on the HCP Assertions signature (wrong Canonicalization method). 
- Bug fix on XDR Request message: field authorPerson was not correct (HTML escape character).
- Member State HomeCommunityId list updated (pn-oid.xml).
  
### Deprecated
- N/A

### Removed
- eID confirmation button has been removed from the OpenNCP Demo Portal.
- OpenATNA logs have been cleaned (verbose and useless information removed). 

## [3.0.0.RC1] - 11-12-2018
### Added
- Support for Wave 2 CDA Implementation Guidelines.

### Changed
- OpenSAML framework upgrade from 2.6.X (not supported anymore) to 3.3.1.
- OpenNCP Portal language properties files are now directly stored in ASCII.
- CDA DisplayTool is now showing original language of the CDA instead of user language

### Deprecated
- N/A

### Removed
- XSPA role "medical doctor" has been removed from the HCP Assertion generator, the role is still accepted by the
default policy.
- Remove generated JAXB class from Evidence Emitter ETSI-REM (created on the fly).

## [2.5.5] - 27-11-2018
### Added
- Audit messages are now supporting multiple ParticipantObjectId (i.e. List of Documents Ids provided).
- OpenNCP Portal property added to set the XSPA Locality to SAML HCP assertions.
- Audit messages for internal NCPs operations added.

### Changed
- Audit messages Participants Identifier aligned with specifications (HP Assurance Audit Schema, Patient Privacy Audit Schema and
Patient ID Mapping Audit Schema).
- eHDSI Gazelle test platform updated (new Schematron WSDL definition).
- OpenNCP Gazelle Validation updated with new Schematron WS client.
- Enabling & disabling feature for Javamelody monitoring tool.
- Definition of a minimal Logging level for Production.
- Update of the OpenNCP Property database schema definition (removing usage of reserved word).
- Alignment of the Audit messages validators use according Event Type and tests definitions.
- Update of the TRC Assertions output based on HCP Assertions provided.
- eHDSI Gazelle Validators are set to Wave 1 requirements (manual customization available)

### Deprecated
- N/A

### Removed
- Removing SchemaLocation attribute definition into CDA handling operations (security).

## [2.5.4]
### Added
### Changed
### Deprecated
### Removed

## [2.5.3]
### Added
### Changed
### Deprecated
### Removed

### [2.5.3.RC3]
### [2.5.3.RC2]
### [2.5.3.RC1]

## [2.5.2]
### Added
### Changed
### Deprecated
### Removed

### [2.5.2.RC5
### [2.5.2.RC4
### [2.5.2.RC3
### [2.5.2.RC2
### [2.5.2.RC1

## [2.5.1]
### Added
### Changed
### Deprecated
### Removed

## [2.5.0]
### Added
### Changed
### Deprecated
### Removed

### [2.5.0.RC1]

## [2.4.3]
### Added
### Changed
### Deprecated
### Removed

## [2.4.2]
### Added
### Changed
### Deprecated
### Removed

## [2.4.1]
### Added
### Changed
### Deprecated
### Removed

## [2.4.0]
### Added
### Changed
### Deprecated
### Removed

## [2.3.0]
### Added
### Changed
### Deprecated
### Removed

## [2.2.1]
### Added
### Changed
### Deprecated
### Removed

## [2.2.0]
### Added
### Changed
### Deprecated
### Removed

## [2.1.1]
### Added
### Changed
### Deprecated
### Removed

## [2.1.0]
### Added
### Changed
### Deprecated
### Removed

## [2.0.2]
### Added
### Changed
### Deprecated
### Removed

## [2.0.1]
### Added
### Changed
### Deprecated
### Removed

## [2.0.0]
### Added
### Changed
### Deprecated
### Removed

## [1.1.3]
### Added
### Changed
### Deprecated
### Removed

## [1.1.2]
### Added
### Changed
### Deprecated
### Removed

## [1.1.1]
### Added
### Changed
### Deprecated
### Removed

## [1.0.0]
### Added
### Changed
### Deprecated
### Removed