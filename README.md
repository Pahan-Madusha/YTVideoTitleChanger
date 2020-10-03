# YTVideoTitleChanger
Change the title of a youtube video periodically to represent timely data

# Configuration
* Refer youtube developer api guidelines and update client_configuration.json
* Include `channelId`, `videoId` and `interval` in `video_details.properties` file
* To run in pre authentication mode, run `AuthHanlder.java` locally and use the output to obtain auth token, refresh token and time to live.

*Refer to Sample properties files*

# Two modes to authenticate
* Authenticate with popup
* Pre authenticate and provide auth token and refresh token in `tokens.properties` file (Useful when running in a remote server where browser authentication is not possible)

# Two modes to run packaged jar
* Provide auth token, refresh token and time to live in `token.properties` file (`java -jar ytube.jar`)
* Provide auth token, refresh token and time to live as arguments (`java -jar ytube.jar <time to live> <auth token> <refresh token>`)