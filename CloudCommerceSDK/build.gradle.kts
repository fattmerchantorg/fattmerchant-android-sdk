val mtf = configurations.create("mtf")
val prod = configurations.create("prod")

artifacts {
    add("mtf", file("./cloud-commerce-sdk-mtf-5.3.1.aar"))
    add("prod", file("./cloud-commerce-sdk-5.3.1.aar"))
}
