package com.sanch.appNotify.core;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class Dtos {
    public record DmRequest(@NotBlank String from, @NotBlank String to, @NotBlank String text) {}
    public record TopicNotifyRequest(@NotBlank String topic, @NotBlank String payload) {}
    public record PrefsRequest(@NotNull List<String> topics) {}
    public record AnnouncementRequest(@NotBlank String text) {}
}