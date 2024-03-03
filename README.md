# :link: Ligoj DigitalOcean plugin [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.ligoj.plugin/plugin-prov-digitalocean/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.ligoj.plugin/plugin-prov-digitalocean)

[![Build Status](https://app.travis-ci.com/github/ligoj/plugin-prov-digitalocean.svg?branch=master)](https://app.travis-ci.com/github/ligoj/plugin-prov-digitalocean)
[![Build Status](https://circleci.com/gh/ligoj/plugin-prov-digitalocean.svg?style=svg)](https://circleci.com/gh/ligoj/plugin-prov-digitalocean)
[![Build Status](https://ci.appveyor.com/api/projects/status/unnurptgv79mqjxg?svg=true)](https://ci.appveyor.com/project/ligoj/plugin-prov-digitalocean/branch/master)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=org.ligoj.plugin%3Aplugin-prov-digitalocean&metric=coverage)](https://sonarcloud.io/dashboard?id=org.ligoj.plugin%3Aplugin-prov-digitalocean)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?metric=alert_status&project=org.ligoj.plugin:plugin-prov-digitalocean)](https://sonarcloud.io/dashboard/index/org.ligoj.plugin:plugin-prov-digitalocean)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/996890fa2ed64d8980e91e18e0a92114)](https://www.codacy.com/gh/ligoj/plugin-prov-digitalocean?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=ligoj/plugin-prov-digitalocean&amp;utm_campaign=Badge_Grade)
[![CodeFactor](https://www.codefactor.io/repository/github/ligoj/plugin-prov-digitalocean/badge)](https://www.codefactor.io/repository/github/ligoj/plugin-prov-digitalocean)
[![License](http://img.shields.io/:license-mit-blue.svg)](http://fabdouglas.mit-license.org/)

[Ligoj](https://github.com/ligoj/ligoj) DigitalOcean provisioning plugin, and extending [Provisioning plugin](https://github.com/ligoj/plugin-prov)
Provides the following features :
- Prices are indirectly read from DigitalOcean console.
- Supported services : Compute, Storage and database

# Subscription parameters
* Access Token

## How to create/get these DigitalOcean parameters?
### Tenant ID/Application ID and Key
Go there: [DigitalOcean API page](https://cloud.digitalocean.com/account/api)
- Click on `Generate New Token`
- Type a token name like `ligoj`
- Unselect the `Write` option if your not willing to use `Terraform`
- Validate the form by clicking on `Generate Token`
- Copy the on-screen generated access token.
