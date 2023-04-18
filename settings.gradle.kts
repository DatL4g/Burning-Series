rootProject.name = "BurningSeries"

include("app")
include("model")
include("network")
include("datastore")
include("datastore-codegen")
include("database")
include("scraper")

include(
    "extension",
    "extension:base",
    "extension:content",
    "extension:background",
    "extension:popup",
)
