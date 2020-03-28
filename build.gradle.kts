plugins {
    java
}

group = "org.learn"
version = "1.0-SNAPSHOT"



repositories {
    mavenCentral()
    flatDir {
        dirs("lib")
    }
}

dependencies {
    testCompile("junit", "junit", "4.12")
    implementation(files("lib/graphicslib3D-1.0.jar"))
    implementation( "org.jogamp.jogl:jogl-all-main:2.3.2" )
    implementation( "org.jogamp.gluegen:gluegen-rt-main:2.3.2" )


}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
