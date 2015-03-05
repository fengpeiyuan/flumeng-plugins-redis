[![Build Status](https://travis-ci.org/fengpeiyuan/flumeng-plugins-redis.png)](https://travis-ci.org/fengpeiyuan/flumeng-plugins-redis)
# Flume-ng plugin with Pull-mode source and Ack-mode sink using Redis List

Flume-ng ([http://flume.apache.org](http://flume.apache.org)). This plugins base on Apache Flume 1.5.0.1 and
Redis 2.8.17.

## Features

* Source list pull-mode using Redis [RPOPLPUSH](http://redisdoc.com/list/rpoplpush.html) command
* Sink with ack-mode using Redis [LREM](http://redis.io/commands/lrem) command

## Usage

* Build or Download jar.
     Checkout and build using ```mvn clean package```
* Copy ```flumeng-plugins-redis-[VERSION].jar``` or ```flumeng-plugins-redis-[VERSION]-jar-with-dependencies.jar``` into your flume
   library path. 
* Copy or create configuration file or sample configuration file to some place(redis list using by source and sink must be the same redis instance).
* Run.
	 Following commend is sample for RedisListPollableSource
	
			bin/flume-ng agent -n agent -c conf -f conf/example-RedisListPollableSource.properties -Dflume.root.logger=DEBUG,console

	 Following command is sample for RedisListPollableSource source and WithRedisListAckSink sink.

			bin/flume-ng agent -n agent -c conf -f conf/example-PullModeSourceAndAckModeSinkWithRedisList.properties

## License

Copyright (c) 2014-2015, Peiyuan Feng <fengpeiyuan@gmail.com>.

This module is licensed under the terms of the BSD license.
Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
