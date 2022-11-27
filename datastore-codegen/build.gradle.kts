plugins {
    kotlin("jvm")
    id("com.google.protobuf")
}

group = "dev.datlag.burningseries.datastore-codegen"

dependencies {
    api("com.google.protobuf:protobuf-java:3.21.8")
    api("io.grpc:grpc-protobuf:1.51.0")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.21.8"
    }
}
