package yt.details.refresher.app;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatistics;
import yt.details.refresher.auth.AuthHandler;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.Properties;
import java.util.TimeZone;

public class TitleChanger
{
    private static String channelId = "";
    private static long interval = 250000L;
    private static String videoId = "";
    private static SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd hh:mm:ss" );
    private static final String timeZone = "IST";
    private static final String tokenPropertyFileName = "tokens.properties";
    private static final String videoDetailsFileName = "video_details.properties";

    public static void main( String[] args ) throws Exception
    {
        AuthHandler authHandler;
        sdf.setTimeZone( TimeZone.getTimeZone( timeZone ) );

        if( args.length == 4 )
        {
            authHandler = new AuthHandler( args[1], args[2], Long.parseLong( args[3] ) );
            interval = Long.parseLong( args[0] );
        }
        else if( args.length == 1 )
        {
            authHandler = new AuthHandler();
            interval = Long.parseLong( args[0] );
        }
        else
        {
            Properties tokens = readPropertiesFiles( tokenPropertyFileName );
            Properties videoDetails = readPropertiesFiles( videoDetailsFileName );
            channelId = videoDetails.getProperty( "channelId" );
            videoId = videoDetails.getProperty( "videoId" );
            interval = Long.parseLong( videoDetails.getProperty( "interval" ) );
            authHandler = new AuthHandler( tokens.getProperty( "token" ), tokens.getProperty( "refreshToken" ), Long.parseLong( tokens.getProperty( "timeToLiveInMillis" ) ) );
        }

        while( true )
        {
            YouTube youtubeService = authHandler.getYoutubeService();
            YouTube.Videos.List request = youtubeService.videos().list( Arrays.asList( "statistics", "snippet" ) );
            VideoListResponse response = request.setId( Collections.singletonList( channelId ) ).execute();

            Optional<Video> optionalVideo = response.getItems().stream().filter( o -> videoId.equals( o.getId() ) ).findFirst();
            if( optionalVideo.isPresent() )
            {
                VideoStatistics statistics = optionalVideo.get().getStatistics();
                VideoSnippet snippet = optionalVideo.get().getSnippet();
                Video video = getVideoToUpdate( statistics, snippet );
                YouTube.Videos videos = youtubeService.videos();
                videos.update( Collections.singletonList( "snippet" ), video ).execute();
                System.out.println( "Updated " );
            }
            else
            {
                System.out.println( "Failed to update" );
                break;
            }
            Thread.sleep( TitleChanger.interval );
        }

    }

    /**
     * Read properties file
     * @param fileName
     * @return
     */
    private static Properties readPropertiesFiles( String fileName )
    {
        Properties prop = null;
        try (FileInputStream fis = new FileInputStream( fileName ))
        {
            prop = new Properties();
            prop.load( fis );
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }
        return prop;
    }

    /**
     * Generate Video object with id, category id and new title
     *
     * @param statistics
     * @param snippet
     * @return
     */
    private static Video getVideoToUpdate( VideoStatistics statistics, VideoSnippet snippet )
    {
        Video video = new Video();
        video.setId( videoId );
        VideoSnippet videoSnippet = new VideoSnippet();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime( new Date() );

        videoSnippet.setTitle( getTitleString( statistics ) );
        videoSnippet.setCategoryId( snippet.getCategoryId() );
        video.setSnippet( videoSnippet );
        return video;
    }

    /**
     * Create video title string with stats and current time
     *
     * @param videoStatistics
     * @return
     */
    private static String getTitleString( VideoStatistics videoStatistics )
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime( new Date() );
        return "This video has " + videoStatistics.getViewCount() + " Views " + videoStatistics.getLikeCount() + " Likes " + videoStatistics.getDislikeCount() + " Dislikes and " + videoStatistics.getCommentCount() + " comments " + "The time now is " + sdf.format( calendar.getTime() ) + " " + timeZone;
    }

}
