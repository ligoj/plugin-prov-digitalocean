"use strict"
/* SOURCE: https://cloud-cdn-digitalocean-com.global.ssl.fastly.net/aurora/assets/aurora-6a0c6202c6b344d842c491ecf1b3c73d.js */
e.DBAAS_DBS=[
{
	id:1,
	name:"PostgreSQL",
	type:"pg",
	images:[{id:3,name:"10"},{id:4,name:"11"}],
	wizardTabs:[
		{id:"create",name:"Create a database cluster",nextButton:"Get Started",noSkip:!0},
		{id:"secure",name:"Secure this database cluster",nextButton:"Allow these inbound sources only",saveOnNext:!0},
		{id:"connect",name:"Connection details",nextButton:"Continue"},
		{id:"next-steps",name:"Next steps",nextButton:"Great, I'm done"}
	]
},{
	id:2,
	name:"MySQL",
	type:"mysql",
	disabled:!t.default.featureEnabled("dbaasMysql"),
	images:[{id:1,name:"8"}],
	navTabs:{connectionPools:!1},wizardTabs:[{id:"create",name:"Create a database cluster",nextButton:"Get Started",noSkip:!0},{id:"secure",name:"Secure this database cluster",nextButton:"Allow these inbound sources only",saveOnNext:!0},{id:"connect",name:"Connection details",nextButton:"Continue"},{id:"next-steps",name:"Next steps",nextButton:"Great, I'm done"}]},{id:3,name:"Redis",type:"redis",disabled:!t.default.featureEnabled("dbaasRedis"),la:!1,hideDefaultDBAndUserMessage:!0,images:[{id:1,name:"5"}],supports:{backups:!1},navTabs:{logs:{label:"Logs"},connectionPools:!1,backups:!1,users:!1,metrics:!!t.default.featureEnabled("dbaasRedisMetrics")},wizardTabs:[{id:"create",name:"Create a database cluster",nextButton:"Get Started",noSkip:!0},{id:"secure",name:"Secure this database cluster",nextButton:"Allow these inbound sources only",saveOnNext:!0},{id:"eviction",name:"Eviction policy",nextButton:"Save eviction policy",saveOnNext:!0},{id:"connect",name:"Connection details",nextButton:"Continue"},{id:"next-steps",name:"Next steps",nextButton:"Great, I'm done"}],logTypes:{queryStats:!1,currentQueryStats:!1},policyTypes:[{name:"noeviction",value:"noeviction",description:"Donâ€™t evict any data, returns error when memory limit is reached."},{name:"allkeys-lru",value:"allkeys_lru",description:"Evict any key, least recently used (LRU) first.",recommended:!0},{name:"allkeys-random",value:"allkeys_random",description:"Evict keys in a random order."},{name:"volatile-lru",value:"volatile_lru",description:"Evict keys with expiration only, least recently used (LRU) first."},{name:"volatile-random",value:"volatile_random",description:"Evict keys with expiration only in a random order."},{name:"volatile-ttl",value:"volatile_ttl",description:"Evict keys with expiration only, shortest time-to-live (TTL) first."}]}]

e.DBAAS_SIZES=KO