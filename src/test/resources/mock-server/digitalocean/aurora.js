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

e.DBAAS_SIZES=[
	{cpu:1,
	monthlyPrice:5,
	memory:1*l,
	doDisk:8,
	disk:10,
	excludeLayouts:["multi_node_2","multi_node_3"]},
	
	{cpu:1,monthlyPrice:10,memory:2*l,doDisk:30,disk:25},
	{cpu:2,monthlyPrice:20,memory:4*l,disk:38,doDisk:80},
	{cpu:4,monthlyPrice:40,memory:8*l,disk:115,doDisk:175},
	{cpu:6,monthlyPrice:80,memory:16*l,disk:270,doDisk:350},
	{cpu:8,monthlyPrice:160,memory:32*l,disk:580,doDisk:700},
	{cpu:16,monthlyPrice:320,memory:64*l,doDisk:1e3,disk:1150,excludeLayouts:["single_node"]}]
e.DBAAS_PRICE_MULTIPLIER=3,
e.DBAAS_PRICE_MULTIPLIER_RO=2
e.DBAAS_PG_MAX_CONNECTIONS_PER_GB=25,e.DBAAS_PG_RESERVED_CONNECTION_POOL_CONNECTIONS=3,e.DBAAS_MYSQL_MAX_CONNECTIONS_PER_GB=75,e.DBAAS_MYSQL_CONNECTIONS_MODIFIER=1,e.DBAAS_REDIS_MAX_CONNECTIONS_PER_GB=4096,e.DBAAS_REDIS_MIN_CONNECTIONS=1e4,e.DBAAS_CLIENT_CONNECTION_LIMIT=5e3,e.DBAAS_MAINTENANCE_TME_SPAN=4,e.DAYS_IN_WEEK=["Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"],e.DBAAS_PG_IDEAL_CACHE_HIT_RATIO_PCT=99,e.DBAAS_PG_IDEAL_INDEX_VS_SEQ_SCANS_RATIO_PCT=99,e.DBAAS_MAX_NAME_LENGTH=63,
e.DBAAS_LAYOUTS=[{name:"Primary only",type:"single_node",meta:"No standby node",priceMultiplier:0},{name:"Primary + Standby",type:"multi_node_2",meta:"One standby node",priceMultiplier:w},{name:"Primary + Two Standbys",type:"multi_node_3",meta:"Two standby nodes",priceMultiplier:2*w,excludeEngines:["redis"]}],
e.CLUSTER_POOL_NAME_MAX_LENGTH=55,e.CLUSTER_POOL_NAME_MIN_LENGTH=3,e.CLUSTER_POOL_NAME_VALIDATION_REGEX=/^[a-z][-a-z0-9]{1,53}[a-z0-9]$/,e.CLUSTER_POOL_NAME_STARTS_WITH_ALPHA_REGEX=/^[a-z]/,e.CLUSTER_POOL_NAME_ENDS_WITH_ALPHANUM_REGEX=/[a-z0-9]$/,e.CLUSTER_POOL_NAME_CONTAINS_REGEX=/^[-a-z0-9]*$/,e.CLUSTER_MAX_NODE_COUNT=512,e.CLUSTER_AUTOSCALE_FIRST_SUPPORTED_VERSION=["1.13.10-do.3","1.14.6-do.3","1.15.3-do.3"],e.CLUSTER_FEATURES={autoscale:"cluster-autoscaler"},
e.CLUSTER_SIZE_CATEGORY_DESCRIPTIONS={Standard:{description:"Balanced with a healthy amount of memory",label:"Standard nodes"},General:{description:"Well-proportioned with dedicated CPU and SSD",label:"General Purpose nodes"},Flexible:{description:"Plans with the same price and varying resources",label:"Flexible nodes"},Optimized:{description:"Dedicated hyper-threading best in class Intel CPUs",label:"CPU-Optimized nodes"}}

/* nbr cp * monthlyPrice*/