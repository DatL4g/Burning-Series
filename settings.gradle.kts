rootProject.name = "BurningSeries"

include("app")
include("model")
include("network")
include("datastore")
include("datastore-codegen")
include("database")

include("extension", "extension:base", "extension:content", "extension:background")
