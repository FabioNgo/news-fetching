package com.microsoft.cognitiveservices.ttssample;

import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import javax.sound.sampled.*;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.*;
import com.google.api.services.youtube.model.*;

public class TTSSample {


    public static class Generator {
        ArrayList<byte[]> audiobuffers = new ArrayList<>();
        ArrayList<AudioInputStream> audioInputStreams = new ArrayList<>();
        ArrayList<String> videoList = new ArrayList<>();
        // path of the wav file
        File wavFile = new File("output\\RecordAudio.wav");

        // format of audio file
        AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;

        public void upload(String media_filename) throws IOException {
            YouTube youtube = getYouTubeService();
            String mime_type = "video/*";
            HashMap<String, String> parameters = new HashMap<>();
            parameters.put("part", "snippet,status");


            Video video = new Video();
            VideoSnippet snippet = new VideoSnippet();
            snippet.set("categoryId", "22");
            snippet.set("description", "Description of uploaded video.");
            snippet.set("title", "Test video upload");
            VideoStatus status = new VideoStatus();
            status.set("privacyStatus", "public");

            video.setSnippet(snippet);
            video.setStatus(status);
            InputStream inputStream = new FileInputStream(new File(media_filename));
            InputStreamContent mediaContent = new InputStreamContent(mime_type, inputStream);
            YouTube.Videos.Insert videosInsertRequest = null;

            videosInsertRequest = youtube.videos().insert(parameters.get("part"), video, mediaContent);

            MediaHttpUploader uploader = videosInsertRequest.getMediaHttpUploader();

            Video response = videosInsertRequest.execute();
            System.out.println(response);

        }

        /**
         * Application name.
         */
        private static final String APPLICATION_NAME = "News Fetching";

        /**
         * Directory to store user credentials for this application.
         */
        private static final java.io.File DATA_STORE_DIR = new java.io.File(
                System.getProperty("user.home"), ".credentials/youtube-java-quickstart");

        /**
         * Global instance of the {@link FileDataStoreFactory}.
         */
        private static FileDataStoreFactory DATA_STORE_FACTORY;

        /**
         * Global instance of the JSON factory.
         */
        private static final JsonFactory JSON_FACTORY =
                JacksonFactory.getDefaultInstance();

        /**
         * Global instance of the HTTP transport.
         */
        private static HttpTransport HTTP_TRANSPORT;

        /**
         * Global instance of the scopes required by this quickstart.
         * <p>
         * If modifying these scopes, delete your previously saved credentials
         * at ~/.credentials/drive-java-quickstart
         */
        private static final List<String> SCOPES =
                Collections.singletonList(YouTubeScopes.YOUTUBE_UPLOAD);

        static {
            try {
                HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
                DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
            } catch (Throwable t) {
                t.printStackTrace();
                System.exit(1);
            }
        }

        /**
         * Create an authorized Credential object.
         *
         * @return an authorized Credential object.
         * @throws IOException
         */
        public com.google.api.client.auth.oauth2.Credential authorize() throws IOException {
            // Load client secrets.
            InputStream in =
                    Generator.class.getResourceAsStream("/client_secret.json");
            GoogleClientSecrets clientSecrets =
                    GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

            // Build flow and trigger user authorization request.
            GoogleAuthorizationCodeFlow flow =
                    new GoogleAuthorizationCodeFlow.Builder(
                            HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                            .setDataStoreFactory(DATA_STORE_FACTORY)
                            .setAccessType("online")
                            .build();
            Credential credential = new AuthorizationCodeInstalledApp(
                    flow, new LocalServerReceiver()).authorize("user");
            return credential;
        }

        /**
         * Build and return an authorized API client service, such as a YouTube
         * Data API client service.
         *
         * @return an authorized API client service
         * @throws IOException
         */
        public YouTube getYouTubeService() throws IOException {
            com.google.api.client.auth.oauth2.Credential credential = authorize();
            return new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        }





    public void main(String... args) throws Exception {
        Scanner scanner = new Scanner(new File("input.txt"));
        int count = 0;
        synchronized (this) {
            while (scanner.hasNext()) {

                String input = scanner.nextLine();
                if (!input.isEmpty()) {
                    count++;
                    System.out.print(count);
                    execute(input);
                    this.wait(7000);
                }
                if (!scanner.hasNext()) {
                    break;
                }
            }

            String filename = concatVideo();
//            cleanUp();
//            upload(filename);
        }
        System.out.println("Done");
        System.exit(0);

    }

    private void cleanUp() {
        File audioDir = new File("output\\audio");
        File videoDir = new File("output\\video");
        Arrays.stream(Objects.requireNonNull(audioDir.listFiles())).filter(File::isFile).forEach(File::delete);
        Arrays.stream(Objects.requireNonNull(videoDir.listFiles())).filter(File::isFile).forEach(File::delete);
    }

    private void executeCommand(String command) {
        System.out.println(command);
        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void createVideo(String videoPath, String audioPath) {
        executeCommand(String.format("ffmpeg.exe -y -i %s  -c:v libx264 -c:a aac -strict experimental -b:a 192k -shortest %s", audioPath, videoPath));
    }

    private void AddTextToVideo(String videoPath, String text) {
        String filter = String.format("drawtext=\"fontfile=C:\\Windows\\Fonts\\arial.ttf:fontsize=20: fontcolor=red:x=10:y=10:text='HELLO'\" ", text);
        executeCommand(String.format("ffmpeg.exe -y -i %s  -vf %s -shortest %s", videoPath, filter, videoPath + "-text.mp4"));
    }

    private String concatVideo() {
        StringBuffer sb = new StringBuffer();
        videoList.forEach(s -> sb.append("file ").append(s.replace("\\", "/")).append('\n'));
        File listVideoFile = new File("list.txt");
        String resultFileName = "";
        try {

            Files.deleteIfExists(listVideoFile.toPath());
            FileWriter fileWriter = new FileWriter(listVideoFile);
            fileWriter.write(sb.toString());
            fileWriter.flush();
            fileWriter.close();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyMMdd-HHmmss");
            resultFileName = String.format("output\\result-%s.mp4",simpleDateFormat.format(Calendar.getInstance().getTime()));
            executeCommand(String.format("ffmpeg -y -f concat -i %s -c copy %s", listVideoFile.getCanonicalPath().replace("\\", "/"),resultFileName ));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultFileName;
    }

    private void mergeAudio() throws IOException, LineUnavailableException, UnsupportedAudioFileException {


    }

    private void execute(String input) {
        System.out.println(input);
        String textToSynthesize = input;
        String outputFormat = AudioOutputFormat.Riff24Khz16BitMonoPcm;
        String deviceLanguage = "vi-VN";
        String genderName = Gender.Male;
        String voiceName = "Microsoft Server Speech Text to Speech Voice (vi-VN, An)";

        try {
            String timeStamp = String.valueOf(System.currentTimeMillis());
            byte[] audioBuffer = TTSService.Synthesize(textToSynthesize, outputFormat, deviceLanguage, genderName, voiceName);
            String outputWave = "output\\audio\\" + timeStamp + ".wav";
            File outputAudio = new File(outputWave);
            FileOutputStream fstream = new FileOutputStream(outputAudio, false);
            fstream.write(audioBuffer);
            fstream.flush();
            fstream.close();
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(outputWave));
            audioInputStreams.add(audioInputStream);
            String videoPath = "output\\video\\" + timeStamp + ".mp4";
            videoList.add(videoPath);
            createVideo(videoPath, outputWave);

//            AddTextToVideo(videoPath,"text");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

    public static void main(String... args) {
        try {
            new Generator().main(args);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
