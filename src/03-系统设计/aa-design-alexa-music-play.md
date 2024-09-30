# Func Requirement and information from OA
Design a system where a customer can ask Alexa to


- Play a Song
- Play songs by an artist
- Play an album by an artist


It's for an SDE II role.

For this specifically if I was familiar with voice parsing I'd have known we needed a language parser to then be able to pass parameters to an API.

Also another thing that was pointed out to me is I should be mindful about a large quantity of results coming from certain searches such as Taylor Swift. So I needed to limit the number of initial results for some of the queries.


# Analysis

这里面有一个部分是有原型的，就是库存管理。



# Non-Func Requirement I guessed
0. Availability & scalability, for sure
1. Low Latency, otherwise user can be frustrating 


## Domain Knowledge
- Music needs to be paid


## Key Entity





# 评论区参考
Capacity estimation:
100 million users
40 million DAU
1 song - 5 MB
500 million songs total
Total storage : 500 million * 5MB = 2.5 PB
1 user listens to 5 songs per day


QPS : 40 million5/2460*60 = 3000 QPS


Non functional requirements :
Low latency
Highly Scalable


Database model :


Choice of DB : Relational
NoSql = Elasticsearch


Tables :


User :
userId ,name,device_id


Song :
song_id , s3url ,duration , genre , launch_date, album,


Artist :


artistId ,name ,country


Album :


album_id , name ,launch_date


artist_songs


artist_id , song_id


Elastic search document :


songId
description
album
artist
duration
genre
launchDate


Here is the HLD :


User gives a command to Alexa to play a song , Alexa sends the message to Transcription service which converts the voice to text . Transcription service sends that converted text to the Elasticsearch cluster where all the metadata of the song (description,album,genre,artist) is stored in a document . ES returns back with the list of songs matching the search criteria order by best match .Transcription service then calls the Main server . Main server calls the subscription service and check whether the user has a membership plan or not . In parellel, main server also query database to get song url of s3 and whether the song belongs to membership required category or not . If the song (best match) needs subscription and user is not a member then system will return back to the user that he need to take membership in order to listen to this song . If in case the user is already a member or song does not belong membership required category then server return back the song id/ids and request CDN to play a song with given ids and CDN plays a song .
There could be few more optimisations done here to make the system scalable such as caching the user info in Redis . We can also cache the song metadata here for a day provided that admin only uploads the new songs to alexa once a day . Upload of songs by admin is not described here as it is out of scope here .


Upvote if like the solution . Open for any feedback which makes the design more scalable .
