(function() {
    const sources = [];
    const videos = document.getElementsByTagName("video");
    if ((videos !== null && videos !== undefined) && videos.length > 0) {
        for (let i = 0; i < videos.length; i++) {
            const video = videos[i];
            video.load();
            video.play();
            video.pause();

            const innerSources = video.getElementsByTagName("source");
            if (video.hasAttribute("src")) {
                const source = video.getAttribute("src");
                if ((source !== null && source !== undefined) && ((source instanceof String || typeof source === "string") && source.trim() !== "") {
                    sources.push(source);
                }
            }

            if ((innerSources !== null && innerSources !== undefined) && innerSources.length > 0) {
                for (let j = 0; j < innerSources.length; j++) {
                    const innerSource = innerSources[j];
                    if (video.hasAttribute("src")) {
                        const innerSrc = innerSource.getAttribute("src");
                        if ((innerSrc !== null && innerSrc !== undefined) && ((innerSrc instanceof String || typeof innerSrc === "string") && innerSrc.trim() !== "") {
                            sources.push(innerSrc);
                        }
                    }
                }
            }
        }

        if ((sources !== null && sources !== undefined) && sources.length > 0) {
            return sources;
        } else {
            return null;
        }
    } else {
        if ((sources !== null && sources !== undefined) && sources.length > 0) {
            return sources;
        } else {
            return null;
        }
    }
})();
