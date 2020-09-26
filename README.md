###### 来源于 google 的 FrameSequence 解码加载GIF图的方案，并适配Glide进行加载

    GlideApp.with(this)
                .asFrame()
                .load("gif")
                .into(imageView);
