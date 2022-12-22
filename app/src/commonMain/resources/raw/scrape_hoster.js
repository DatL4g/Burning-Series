(function() {
    const hosterTabs = document.getElementsByClassName("hoster-tabs");
    let activeHoster = null;
    if ((hosterTabs !== null && hosterTabs !== undefined) && hosterTabs.length > 0) {
        const activeTabs = hosterTabs[0].getElementsByClassName("active");
        if ((activeTabs !== null && activeTabs !== undefined) && activeTabs.length > 0) {
            const hosters = activeTabs[0].getElementsByTagName("a");
            if ((hosters !== null && hosters !== undefined) && hosters.length > 0) {
                activeHoster = hosters[0].getAttribute("href");
            }
        }
    }

    const hosterPlayers = document.getElementsByClassName("hoster-player");
    if ((hosterPlayers !== null && hosterPlayers !== undefined) && hosterPlayers.length > 0) {
        const player = hosterPlayers[0];
        const links = player.getElementsByTagName("a");
        if ((links !== null && links !== undefined) && links.length > 0) {
            const link = links[0].getAttribute("href");
            return {href: activeHoster, url: link, embed: false};
        } else {
            const frames = player.getElementsByTagName("iframe");
            if ((frames !== null && frames !== undefined) && frames.length > 0) {
                const link = frames[0].getAttribute("src");
                return {href: activeHoster, url: link, embed: true};
            } else {
                return null;
            }
        }
    }
})();
