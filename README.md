# DiscordBot
Disclaimer(this is moreso a random wall of thoughts and ideas rather than a README for instructions...) You've been warned.

First go at a more in depth Discord bot using Java instead of node.js (last time was just an Elon bot that would just spout random things out without real logic behind him.

http://localhost:4567/userstats/ and then the unique snowflake id to locally grab or send a get request on the application against the Redis cache to see if the user data is available.

just call .lastgame or .resetgame to get stats after the bot records the stats

used listeneradapters instead of eventlisteners (we're only listening for a few things anyways why implement them all)

overall structure could use work, didn't realize how simple what was asked for but I was thinking of things like LRU/eviction/compression/algorithms to handle optimizations/etc

things like games changing such as league of legends or spotify or osu! get iffy, when they keep changing game states but type is same (handled for the most part by checking against redis but maybe create a local cache if requests get too heavy)

cachedthreadpool can introduce security issues as well as crippling issues where unnecessary amounts of threads could be called to be issuing or crippling resources for heavier servers that are requesting more

fixed threads chceking the queue size and seeing if there are alot of tasks waiting to be accomplished then adding more threads or increasing thread limit size for a specific space

jconsole, mysql workbench, docker, redis, apache, guava, spark, other opensource stuff

need more testing but there's alot of awesome and exciting structures that are faster or slower or more durable and have definite tradeoffs.

bytes vs strings vs objects

LRU/LFU with redis / polling / offline,online status changes for pushes / expiration of keys TTL,expiration / batch of entries pushed through snapshot file or pipelining or transactions... thoughts everywhere

restructure a few portions where it could be communicated better and actually implement Domain Driven Design with factory and actual OOP.

In terms of design I was thinking that hm, I wondering how the guys are indexing and storing things. My thought was holding users based off their guild and unique id (snowflake id) discord gives them. As far as I know there's rate limitations from Discord which are handled by JDA's queue system but if the bot has more intensive tasks and we go away from pulling from the newCachedThreadPool, we model a certain level of metrics which is distributed and recorded overtime to allocate (in an intelligent way through ML) we can preallocate and optimize the backend infrastructure where threads are traveled. Since pulling from newCacheThreadPool can have severe security to performance flunks at times ie. outside requests / webhooks / stale threads. So if a DOS attack happened through the many ways of the internet, the server code will be better serviced (of course if there were fail-safe or blocking mechanisms in place). Work is work. Another is if a server gets saturated with too many requests/threads and it hits a peak, the bot should finish tasks first within milliseconds instead of timing out requests due to oversaturation of the CPU on the server. So a fixed thread pool and increasing based off a model using ML / workload / popularity basis with overhead. Granted since queuing makes things difficult anyways to optimize/tune things than other structures... Sure we have futures and scheduled executions but those should be in separate thread pools. I have a ThreadFactory as default but can be better configured to check server metrics as well. Just random thoughts and exciting reads ...

Wall of text over! Compilation of thoughts and whatnot