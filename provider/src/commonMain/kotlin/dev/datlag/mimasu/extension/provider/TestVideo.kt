package dev.datlag.mimasu.extension.provider

import io.ktor.http.parseUrl

internal data object TestVideo {

    private val testVideoHosts = setOf(
        "test-videos.co.uk",
        "test-streams.mux.dev",
        "bbb3d.renderfarming.net"
    )

    private val testVideos = setOf(
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/av1/360/Big_Buck_Bunny_360_10s_1MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/av1/720/Big_Buck_Bunny_720_10s_2MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/av1/1080/Big_Buck_Bunny_1080_10s_5MB.mp4",

        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/360/Big_Buck_Bunny_360_10s_1MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/720/Big_Buck_Bunny_720_10s_2MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/1080/Big_Buck_Bunny_1080_10s_5MB.mp4",

        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp9/360/Big_Buck_Bunny_360_10s_1MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp9/720/Big_Buck_Bunny_720_10s_2MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp9/1080/Big_Buck_Bunny_1080_10s_5MB.webm",

        "https://test-videos.co.uk/vids/bigbuckbunny/mkv/360/Big_Buck_Bunny_360_10s_1MB.mkv",
        "https://test-videos.co.uk/vids/bigbuckbunny/mkv/720/Big_Buck_Bunny_720_10s_2MB.mkv",
        "https://test-videos.co.uk/vids/bigbuckbunny/mkv/1080/Big_Buck_Bunny_1080_10s_5MB.mkv",

        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/1080/Big_Buck_Bunny_1080_10s_1MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/1080/Big_Buck_Bunny_1080_10s_2MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/1080/Big_Buck_Bunny_1080_10s_5MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/1080/Big_Buck_Bunny_1080_10s_10MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/1080/Big_Buck_Bunny_1080_10s_20MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/1080/Big_Buck_Bunny_1080_10s_30MB.mp4",

        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/720/Big_Buck_Bunny_720_10s_1MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/720/Big_Buck_Bunny_720_10s_2MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/720/Big_Buck_Bunny_720_10s_5MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/720/Big_Buck_Bunny_720_10s_10MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/720/Big_Buck_Bunny_720_10s_20MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/720/Big_Buck_Bunny_720_10s_30MB.mp4",

        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/360/Big_Buck_Bunny_360_10s_1MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/360/Big_Buck_Bunny_360_10s_2MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/360/Big_Buck_Bunny_360_10s_5MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/360/Big_Buck_Bunny_360_10s_10MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/360/Big_Buck_Bunny_360_10s_20MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/360/Big_Buck_Bunny_360_10s_30MB.mp4",

        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h265/1080/Big_Buck_Bunny_1080_10s_1MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h265/1080/Big_Buck_Bunny_1080_10s_2MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h265/1080/Big_Buck_Bunny_1080_10s_5MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h265/1080/Big_Buck_Bunny_1080_10s_10MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h265/1080/Big_Buck_Bunny_1080_10s_20MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h265/1080/Big_Buck_Bunny_1080_10s_30MB.mp4",

        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h265/720/Big_Buck_Bunny_720_10s_1MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h265/720/Big_Buck_Bunny_720_10s_2MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h265/720/Big_Buck_Bunny_720_10s_5MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h265/720/Big_Buck_Bunny_720_10s_10MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h265/720/Big_Buck_Bunny_720_10s_20MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h265/720/Big_Buck_Bunny_720_10s_30MB.mp4",

        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h265/360/Big_Buck_Bunny_360_10s_1MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h265/360/Big_Buck_Bunny_360_10s_2MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h265/360/Big_Buck_Bunny_360_10s_5MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h265/360/Big_Buck_Bunny_360_10s_10MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h265/360/Big_Buck_Bunny_360_10s_20MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h265/360/Big_Buck_Bunny_360_10s_28MB.mp4",

        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/av1/1080/Big_Buck_Bunny_1080_10s_1MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/av1/1080/Big_Buck_Bunny_1080_10s_2MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/av1/1080/Big_Buck_Bunny_1080_10s_5MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/av1/1080/Big_Buck_Bunny_1080_10s_10MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/av1/1080/Big_Buck_Bunny_1080_10s_20MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/av1/1080/Big_Buck_Bunny_1080_10s_30MB.mp4",

        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/av1/720/Big_Buck_Bunny_720_10s_1MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/av1/720/Big_Buck_Bunny_720_10s_2MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/av1/720/Big_Buck_Bunny_720_10s_5MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/av1/720/Big_Buck_Bunny_720_10s_10MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/av1/720/Big_Buck_Bunny_720_10s_20MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/av1/720/Big_Buck_Bunny_720_10s_30MB.mp4",

        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/av1/360/Big_Buck_Bunny_360_10s_1MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/av1/360/Big_Buck_Bunny_360_10s_2MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/av1/360/Big_Buck_Bunny_360_10s_5MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/av1/360/Big_Buck_Bunny_360_10s_10MB.mp4",
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/av1/360/Big_Buck_Bunny_360_10s_20MB.mp4",

        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp8/1080/Big_Buck_Bunny_1080_10s_1MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp8/1080/Big_Buck_Bunny_1080_10s_2MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp8/1080/Big_Buck_Bunny_1080_10s_5MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp8/1080/Big_Buck_Bunny_1080_10s_10MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp8/1080/Big_Buck_Bunny_1080_10s_20MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp8/1080/Big_Buck_Bunny_1080_10s_30MB.webm",

        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp8/720/Big_Buck_Bunny_720_10s_1MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp8/720/Big_Buck_Bunny_720_10s_2MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp8/720/Big_Buck_Bunny_720_10s_5MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp8/720/Big_Buck_Bunny_720_10s_10MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp8/720/Big_Buck_Bunny_720_10s_20MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp8/720/Big_Buck_Bunny_720_10s_30MB.webm",

        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp8/360/Big_Buck_Bunny_360_10s_1MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp8/360/Big_Buck_Bunny_360_10s_2MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp8/360/Big_Buck_Bunny_360_10s_5MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp8/360/Big_Buck_Bunny_360_10s_10MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp8/360/Big_Buck_Bunny_360_10s_20MB.webm",

        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp9/1080/Big_Buck_Bunny_1080_10s_1MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp9/1080/Big_Buck_Bunny_1080_10s_2MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp9/1080/Big_Buck_Bunny_1080_10s_5MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp9/1080/Big_Buck_Bunny_1080_10s_10MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp9/1080/Big_Buck_Bunny_1080_10s_20MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp9/1080/Big_Buck_Bunny_1080_10s_30MB.webm",

        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp9/720/Big_Buck_Bunny_720_10s_1MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp9/720/Big_Buck_Bunny_720_10s_2MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp9/720/Big_Buck_Bunny_720_10s_5MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp9/720/Big_Buck_Bunny_720_10s_10MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp9/720/Big_Buck_Bunny_720_10s_20MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp9/720/Big_Buck_Bunny_720_10s_30MB.webm",

        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp9/360/Big_Buck_Bunny_360_10s_1MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp9/360/Big_Buck_Bunny_360_10s_2MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp9/360/Big_Buck_Bunny_360_10s_5MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp9/360/Big_Buck_Bunny_360_10s_10MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp9/360/Big_Buck_Bunny_360_10s_20MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/vp9/360/Big_Buck_Bunny_360_10s_30MB.webm",

        "https://test-videos.co.uk/vids/bigbuckbunny/webm/av1/1080/Big_Buck_Bunny_1080_10s_1MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/av1/1080/Big_Buck_Bunny_1080_10s_2MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/av1/1080/Big_Buck_Bunny_1080_10s_5MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/av1/1080/Big_Buck_Bunny_1080_10s_10MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/av1/1080/Big_Buck_Bunny_1080_10s_20MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/av1/1080/Big_Buck_Bunny_1080_10s_30MB.webm",

        "https://test-videos.co.uk/vids/bigbuckbunny/webm/av1/720/Big_Buck_Bunny_720_10s_1MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/av1/720/Big_Buck_Bunny_720_10s_2MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/av1/720/Big_Buck_Bunny_720_10s_5MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/av1/720/Big_Buck_Bunny_720_10s_10MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/av1/720/Big_Buck_Bunny_720_10s_20MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/av1/720/Big_Buck_Bunny_720_10s_30MB.webm",

        "https://test-videos.co.uk/vids/bigbuckbunny/webm/av1/360/Big_Buck_Bunny_360_10s_1MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/av1/360/Big_Buck_Bunny_360_10s_2MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/av1/360/Big_Buck_Bunny_360_10s_5MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/av1/360/Big_Buck_Bunny_360_10s_10MB.webm",
        "https://test-videos.co.uk/vids/bigbuckbunny/webm/av1/360/Big_Buck_Bunny_360_10s_20MB.webm",

        "https://test-videos.co.uk/vids/bigbuckbunny/mkv/1080/Big_Buck_Bunny_1080_10s_1MB.mkv",
        "https://test-videos.co.uk/vids/bigbuckbunny/mkv/1080/Big_Buck_Bunny_1080_10s_2MB.mkv",
        "https://test-videos.co.uk/vids/bigbuckbunny/mkv/1080/Big_Buck_Bunny_1080_10s_5MB.mkv",
        "https://test-videos.co.uk/vids/bigbuckbunny/mkv/1080/Big_Buck_Bunny_1080_10s_10MB.mkv",
        "https://test-videos.co.uk/vids/bigbuckbunny/mkv/1080/Big_Buck_Bunny_1080_10s_20MB.mkv",
        "https://test-videos.co.uk/vids/bigbuckbunny/mkv/1080/Big_Buck_Bunny_1080_10s_30MB.mkv",

        "https://test-videos.co.uk/vids/bigbuckbunny/mkv/720/Big_Buck_Bunny_720_10s_1MB.mkv",
        "https://test-videos.co.uk/vids/bigbuckbunny/mkv/720/Big_Buck_Bunny_720_10s_2MB.mkv",
        "https://test-videos.co.uk/vids/bigbuckbunny/mkv/720/Big_Buck_Bunny_720_10s_5MB.mkv",
        "https://test-videos.co.uk/vids/bigbuckbunny/mkv/720/Big_Buck_Bunny_720_10s_10MB.mkv",
        "https://test-videos.co.uk/vids/bigbuckbunny/mkv/720/Big_Buck_Bunny_720_10s_20MB.mkv",
        "https://test-videos.co.uk/vids/bigbuckbunny/mkv/720/Big_Buck_Bunny_720_10s_30MB.mkv",

        "https://test-videos.co.uk/vids/bigbuckbunny/mkv/360/Big_Buck_Bunny_360_10s_1MB.mkv",
        "https://test-videos.co.uk/vids/bigbuckbunny/mkv/360/Big_Buck_Bunny_360_10s_2MB.mkv",
        "https://test-videos.co.uk/vids/bigbuckbunny/mkv/360/Big_Buck_Bunny_360_10s_5MB.mkv",
        "https://test-videos.co.uk/vids/bigbuckbunny/mkv/360/Big_Buck_Bunny_360_10s_10MB.mkv",
        "https://test-videos.co.uk/vids/bigbuckbunny/mkv/360/Big_Buck_Bunny_360_10s_20MB.mkv",
        "https://test-videos.co.uk/vids/bigbuckbunny/mkv/360/Big_Buck_Bunny_360_10s_30MB.mkv",

        "https://test-videos.co.uk/vids/jellyfish/mp4/h264/1080/Jellyfish_1080_10s_1MB.mp4",
        "https://test-videos.co.uk/vids/jellyfish/mp4/h264/1080/Jellyfish_1080_10s_2MB.mp4",
        "https://test-videos.co.uk/vids/jellyfish/mp4/h264/1080/Jellyfish_1080_10s_5MB.mp4",
        "https://test-videos.co.uk/vids/jellyfish/mp4/h264/1080/Jellyfish_1080_10s_10MB.mp4",
        "https://test-videos.co.uk/vids/jellyfish/mp4/h264/1080/Jellyfish_1080_10s_20MB.mp4",
        "https://test-videos.co.uk/vids/jellyfish/mp4/h264/1080/Jellyfish_1080_10s_30MB.mp4",

        "https://test-videos.co.uk/vids/jellyfish/mp4/h264/720/Jellyfish_720_10s_1MB.mp4",
        "https://test-videos.co.uk/vids/jellyfish/mp4/h264/720/Jellyfish_720_10s_2MB.mp4",
        "https://test-videos.co.uk/vids/jellyfish/mp4/h264/720/Jellyfish_720_10s_5MB.mp4",
        "https://test-videos.co.uk/vids/jellyfish/mp4/h264/720/Jellyfish_720_10s_10MB.mp4",
        "https://test-videos.co.uk/vids/jellyfish/mp4/h264/720/Jellyfish_720_10s_20MB.mp4",
        "https://test-videos.co.uk/vids/jellyfish/mp4/h264/720/Jellyfish_720_10s_30MB.mp4",

        "https://test-videos.co.uk/vids/jellyfish/mp4/h264/360/Jellyfish_360_10s_1MB.mp4",
        "https://test-videos.co.uk/vids/jellyfish/mp4/h264/360/Jellyfish_360_10s_2MB.mp4",
        "https://test-videos.co.uk/vids/jellyfish/mp4/h264/360/Jellyfish_360_10s_5MB.mp4",
        "https://test-videos.co.uk/vids/jellyfish/mp4/h264/360/Jellyfish_360_10s_10MB.mp4",
        "https://test-videos.co.uk/vids/jellyfish/mp4/h264/360/Jellyfish_360_10s_20MB.mp4",
        "https://test-videos.co.uk/vids/jellyfish/mp4/h264/360/Jellyfish_360_10s_30MB.mp4",

        "https://test-videos.co.uk/vids/jellyfish/mp4/h265/1080/Jellyfish_1080_10s_1MB.mp4",
        "https://test-videos.co.uk/vids/jellyfish/mp4/h265/1080/Jellyfish_1080_10s_2MB.mp4",
        "https://test-videos.co.uk/vids/jellyfish/mp4/h265/1080/Jellyfish_1080_10s_5MB.mp4",
        "https://test-videos.co.uk/vids/jellyfish/mp4/h265/1080/Jellyfish_1080_10s_10MB.mp4",
        "https://test-videos.co.uk/vids/jellyfish/mp4/h265/1080/Jellyfish_1080_10s_20MB.mp4",
        "https://test-videos.co.uk/vids/jellyfish/mp4/h265/1080/Jellyfish_1080_10s_30MB.mp4",

        "https://test-videos.co.uk/vids/jellyfish/mp4/h265/720/Jellyfish_720_10s_1MB.mp4",
        "https://test-videos.co.uk/vids/jellyfish/mp4/h265/720/Jellyfish_720_10s_2MB.mp4",
        "https://test-videos.co.uk/vids/jellyfish/mp4/h265/720/Jellyfish_720_10s_5MB.mp4",
        "https://test-videos.co.uk/vids/jellyfish/mp4/h265/720/Jellyfish_720_10s_10MB.mp4",
        "https://test-videos.co.uk/vids/jellyfish/mp4/h265/720/Jellyfish_720_10s_20MB.mp4",
        "https://test-videos.co.uk/vids/jellyfish/mp4/h265/720/Jellyfish_720_10s_30MB.mp4",

        "https://test-videos.co.uk/vids/jellyfish/mp4/h265/360/Jellyfish_360_10s_1MB.mp4",
        "https://test-videos.co.uk/vids/jellyfish/mp4/h265/360/Jellyfish_360_10s_2MB.mp4",
        "https://test-videos.co.uk/vids/jellyfish/mp4/h265/360/Jellyfish_360_10s_5MB.mp4",
        "https://test-videos.co.uk/vids/jellyfish/mp4/h265/360/Jellyfish_360_10s_10MB.mp4",
        "https://test-videos.co.uk/vids/jellyfish/mp4/h265/360/Jellyfish_360_10s_20MB.mp4",
        "https://test-videos.co.uk/vids/jellyfish/mp4/h265/360/Jellyfish_360_10s_30MB.mp4",

        "https://test-videos.co.uk/vids/jellyfish/webm/vp8/1080/Jellyfish_1080_10s_1MB.webm",
        "https://test-videos.co.uk/vids/jellyfish/webm/vp8/1080/Jellyfish_1080_10s_2MB.webm",
        "https://test-videos.co.uk/vids/jellyfish/webm/vp8/1080/Jellyfish_1080_10s_5MB.webm",
        "https://test-videos.co.uk/vids/jellyfish/webm/vp8/1080/Jellyfish_1080_10s_10MB.webm",
        "https://test-videos.co.uk/vids/jellyfish/webm/vp8/1080/Jellyfish_1080_10s_20MB.webm",
        "https://test-videos.co.uk/vids/jellyfish/webm/vp8/1080/Jellyfish_1080_10s_30MB.webm",

        "https://test-videos.co.uk/vids/jellyfish/webm/vp8/720/Jellyfish_720_10s_1MB.webm",
        "https://test-videos.co.uk/vids/jellyfish/webm/vp8/720/Jellyfish_720_10s_2MB.webm",
        "https://test-videos.co.uk/vids/jellyfish/webm/vp8/720/Jellyfish_720_10s_5MB.webm",
        "https://test-videos.co.uk/vids/jellyfish/webm/vp8/720/Jellyfish_720_10s_10MB.webm",
        "https://test-videos.co.uk/vids/jellyfish/webm/vp8/720/Jellyfish_720_10s_20MB.webm",
        "https://test-videos.co.uk/vids/jellyfish/webm/vp8/720/Jellyfish_720_10s_30MB.webm",

        "https://test-videos.co.uk/vids/jellyfish/webm/vp8/360/Jellyfish_360_10s_1MB.webm",
        "https://test-videos.co.uk/vids/jellyfish/webm/vp8/360/Jellyfish_360_10s_2MB.webm",
        "https://test-videos.co.uk/vids/jellyfish/webm/vp8/360/Jellyfish_360_10s_5MB.webm",
        "https://test-videos.co.uk/vids/jellyfish/webm/vp8/360/Jellyfish_360_10s_10MB.webm",
        "https://test-videos.co.uk/vids/jellyfish/webm/vp8/360/Jellyfish_360_10s_20MB.webm",
        "https://test-videos.co.uk/vids/jellyfish/webm/vp8/360/Jellyfish_360_10s_24MB.webm",

        "https://test-videos.co.uk/vids/jellyfish/webm/vp9/1080/Jellyfish_1080_10s_1MB.webm",
        "https://test-videos.co.uk/vids/jellyfish/webm/vp9/1080/Jellyfish_1080_10s_2MB.webm",
        "https://test-videos.co.uk/vids/jellyfish/webm/vp9/1080/Jellyfish_1080_10s_5MB.webm",
        "https://test-videos.co.uk/vids/jellyfish/webm/vp9/1080/Jellyfish_1080_10s_10MB.webm",
        "https://test-videos.co.uk/vids/jellyfish/webm/vp9/1080/Jellyfish_1080_10s_20MB.webm",
        "https://test-videos.co.uk/vids/jellyfish/webm/vp9/1080/Jellyfish_1080_10s_30MB.webm",

        "https://test-videos.co.uk/vids/jellyfish/webm/vp9/720/Jellyfish_720_10s_1MB.webm",
        "https://test-videos.co.uk/vids/jellyfish/webm/vp9/720/Jellyfish_720_10s_2MB.webm",
        "https://test-videos.co.uk/vids/jellyfish/webm/vp9/720/Jellyfish_720_10s_5MB.webm",
        "https://test-videos.co.uk/vids/jellyfish/webm/vp9/720/Jellyfish_720_10s_10MB.webm",
        "https://test-videos.co.uk/vids/jellyfish/webm/vp9/720/Jellyfish_720_10s_20MB.webm",
        "https://test-videos.co.uk/vids/jellyfish/webm/vp9/720/Jellyfish_720_10s_30MB.webm",

        "https://test-videos.co.uk/vids/jellyfish/webm/vp9/360/Jellyfish_360_10s_1MB.webm",
        "https://test-videos.co.uk/vids/jellyfish/webm/vp9/360/Jellyfish_360_10s_2MB.webm",
        "https://test-videos.co.uk/vids/jellyfish/webm/vp9/360/Jellyfish_360_10s_5MB.webm",
        "https://test-videos.co.uk/vids/jellyfish/webm/vp9/360/Jellyfish_360_10s_10MB.webm",
        "https://test-videos.co.uk/vids/jellyfish/webm/vp9/360/Jellyfish_360_10s_20MB.webm",
        "https://test-videos.co.uk/vids/jellyfish/webm/vp9/360/Jellyfish_360_10s_30MB.webm",

        "https://test-videos.co.uk/vids/jellyfish/mkv/1080/Jellyfish_1080_10s_1MB.mkv",
        "https://test-videos.co.uk/vids/jellyfish/mkv/1080/Jellyfish_1080_10s_2MB.mkv",
        "https://test-videos.co.uk/vids/jellyfish/mkv/1080/Jellyfish_1080_10s_5MB.mkv",
        "https://test-videos.co.uk/vids/jellyfish/mkv/1080/Jellyfish_1080_10s_10MB.mkv",
        "https://test-videos.co.uk/vids/jellyfish/mkv/1080/Jellyfish_1080_10s_20MB.mkv",
        "https://test-videos.co.uk/vids/jellyfish/mkv/1080/Jellyfish_1080_10s_30MB.mkv",

        "https://test-videos.co.uk/vids/jellyfish/mkv/720/Jellyfish_720_10s_1MB.mkv",
        "https://test-videos.co.uk/vids/jellyfish/mkv/720/Jellyfish_720_10s_2MB.mkv",
        "https://test-videos.co.uk/vids/jellyfish/mkv/720/Jellyfish_720_10s_5MB.mkv",
        "https://test-videos.co.uk/vids/jellyfish/mkv/720/Jellyfish_720_10s_10MB.mkv",
        "https://test-videos.co.uk/vids/jellyfish/mkv/720/Jellyfish_720_10s_20MB.mkv",
        "https://test-videos.co.uk/vids/jellyfish/mkv/720/Jellyfish_720_10s_30MB.mkv",

        "https://test-videos.co.uk/vids/jellyfish/mkv/360/Jellyfish_360_10s_1MB.mkv",
        "https://test-videos.co.uk/vids/jellyfish/mkv/360/Jellyfish_360_10s_2MB.mkv",
        "https://test-videos.co.uk/vids/jellyfish/mkv/360/Jellyfish_360_10s_5MB.mkv",
        "https://test-videos.co.uk/vids/jellyfish/mkv/360/Jellyfish_360_10s_10MB.mkv",
        "https://test-videos.co.uk/vids/jellyfish/mkv/360/Jellyfish_360_10s_20MB.mkv",
        "https://test-videos.co.uk/vids/jellyfish/mkv/360/Jellyfish_360_10s_30MB.mkv",

        "https://test-videos.co.uk/vids/sintel/mp4/av1/1080/Sintel_1080_10s_1MB.mp4",
        "https://test-videos.co.uk/vids/sintel/mp4/av1/1080/Sintel_1080_10s_2MB.mp4",
        "https://test-videos.co.uk/vids/sintel/mp4/av1/1080/Sintel_1080_10s_5MB.mp4",
        "https://test-videos.co.uk/vids/sintel/mp4/av1/1080/Sintel_1080_10s_10MB.mp4",
        "https://test-videos.co.uk/vids/sintel/mp4/av1/1080/Sintel_1080_10s_20MB.mp4",
        "https://test-videos.co.uk/vids/sintel/mp4/av1/1080/Sintel_1080_10s_30MB.mp4",

        "https://test-videos.co.uk/vids/sintel/mp4/av1/720/Sintel_720_10s_1MB.mp4",
        "https://test-videos.co.uk/vids/sintel/mp4/av1/720/Sintel_720_10s_2MB.mp4",
        "https://test-videos.co.uk/vids/sintel/mp4/av1/720/Sintel_720_10s_5MB.mp4",
        "https://test-videos.co.uk/vids/sintel/mp4/av1/720/Sintel_720_10s_10MB.mp4",
        "https://test-videos.co.uk/vids/sintel/mp4/av1/720/Sintel_720_10s_20MB.mp4",
        "https://test-videos.co.uk/vids/sintel/mp4/av1/720/Sintel_720_10s_30MB.mp4",

        "https://test-videos.co.uk/vids/sintel/mp4/av1/360/Sintel_360_10s_1MB.mp4",
        "https://test-videos.co.uk/vids/sintel/mp4/av1/360/Sintel_360_10s_2MB.mp4",
        "https://test-videos.co.uk/vids/sintel/mp4/av1/360/Sintel_360_10s_5MB.mp4",
        "https://test-videos.co.uk/vids/sintel/mp4/av1/360/Sintel_360_10s_10MB.mp4",

        "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
        "https://test-streams.mux.dev/x36xhzz/url_6/193039199_mp4_h264_aac_hq_7.m3u8",
        "https://test-streams.mux.dev/test_001/stream.m3u8",
        "https://test-streams.mux.dev/dai-discontinuity-deltatre/manifest.m3u8",
        "https://test-streams.mux.dev/issue666/playlists/cisq0gim60007xzvi505emlxx.m3u8",
        "https://test-streams.mux.dev/bbbAES/playlists/sample_aes/index.m3u8",
        "https://test-streams.mux.dev/pts_shift/master.m3u8",
        "https://test-streams.mux.dev/tos_ismc/main.m3u8",

        "http://distribution.bbb3d.renderfarming.net/video/mp4/bbb_sunflower_1080p_60fps_stereo_abl.mp4",
        "http://distribution.bbb3d.renderfarming.net/video/mp4/bbb_sunflower_1080p_30fps_stereo_abl.mp4",

        "http://distribution.bbb3d.renderfarming.net/video/mp4/bbb_sunflower_2160p_60fps_stereo_abl.mp4",
        "http://distribution.bbb3d.renderfarming.net/video/mp4/bbb_sunflower_2160p_30fps_stereo_abl.mp4",

        "http://distribution.bbb3d.renderfarming.net/video/mp4/bbb_sunflower_native_60fps_stereo_abl.mp4",

        "http://distribution.bbb3d.renderfarming.net/video/mp4/bbb_sunflower_1080p_60fps_stereo_arcd.mp4",
        "http://distribution.bbb3d.renderfarming.net/video/mp4/bbb_sunflower_1080p_60fps_stereo_arcc.mp4",
        "http://distribution.bbb3d.renderfarming.net/video/mp4/bbb_sunflower_1080p_60fps_stereo_agmh.mp4",

        "http://distribution.bbb3d.renderfarming.net/video/mp4/bbb_sunflower_1080p_30fps_stereo_arcd.mp4",
        "http://distribution.bbb3d.renderfarming.net/video/mp4/bbb_sunflower_1080p_30fps_stereo_arcc.mp4",
        "http://distribution.bbb3d.renderfarming.net/video/mp4/bbb_sunflower_1080p_30fps_stereo_agmh.mp4",

        "http://download.blender.org/peach/bigbuckbunny_movies/big_buck_bunny_480p_surround-fix.avi",
        "http://mirror.bigbuckbunny.de/peach/bigbuckbunny_movies/big_buck_bunny_720p_surround.avi",

        "http://distribution.bbb3d.renderfarming.net/video/mp4/bbb_sunflower_1080p_60fps_normal.mp4",
        "http://distribution.bbb3d.renderfarming.net/video/mp4/bbb_sunflower_1080p_30fps_normal.mp4",

        "http://distribution.bbb3d.renderfarming.net/video/mp4/bbb_sunflower_2160p_60fps_normal.mp4",
        "http://distribution.bbb3d.renderfarming.net/video/mp4/bbb_sunflower_2160p_30fps_normal.mp4",

        "http://distribution.bbb3d.renderfarming.net/video/mp4/bbb_sunflower_native_60fps_normal.mp4"
    )

    fun filter(list: Collection<String>): List<String> {
        return list.filterNot { link ->
            testVideos.any { it == link }
        }.filterNot { link ->
            val url = parseUrl(link) ?: return@filterNot false

            testVideoHosts.any {
                it.equals(url.host, ignoreCase = true)
            }
        }
    }
}