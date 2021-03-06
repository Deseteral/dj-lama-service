package xyz.deseteral.djlama.discord;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import xyz.deseteral.djlama.queue.Queue;
import xyz.deseteral.djlama.song.Song;
import xyz.deseteral.djlama.song.SongService;

import java.time.Instant;

public class TrackScheduler extends AudioEventAdapter {
    private Queue queue;
    private SongService songService;
    private AudioPlayer player;
    private AudioPlayerManager playerManager;
    private Song currentlyPlaying;
    private Instant startTime = null;

    TrackScheduler(Queue queue, SongService songService, AudioPlayer player, AudioPlayerManager playerManager) {
        this.queue = queue;
        this.songService = songService;
        this.player = player;
        this.playerManager = playerManager;
    }

    void playNext() {
        currentlyPlaying = queue.pop();
        if (currentlyPlaying == null) {
            return;
        }

        playerManager.loadItem(currentlyPlaying.getYoutubeId(), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                player.playTrack(track);

                songService.markAsPlayed(currentlyPlaying.getId());

                if (startTime == null) {
                    startTime = Instant.now();
                }
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) { }

            @Override
            public void noMatches() { }

            @Override
            public void loadFailed(FriendlyException throwable) { }
        });
    }

    Song getCurrentlyPlaying() {
        return currentlyPlaying;
    }

    Instant getStartTime() {
        return startTime;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack t, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            playNext();
        }
    }

    @Override
    public void onPlayerPause(AudioPlayer player) { }

    @Override
    public void onPlayerResume(AudioPlayer player) { }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) { }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) { }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) { }
}
