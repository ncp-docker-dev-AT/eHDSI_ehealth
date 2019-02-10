# Change Log

All notable changes to the <a href="https://ec.europa.eu/cefdigital/wiki/x/8CEZAg" rel="nofollow">CEF EHDSI project</a> will be documented in this file.<br/> 
This project adheres to <a href="http://semver.org/" rel="nofollow">Semantic Versioning</a>.

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