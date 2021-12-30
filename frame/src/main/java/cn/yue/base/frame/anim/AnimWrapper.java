package cn.yue.base.frame.anim;

import java.util.ArrayList;
import java.util.List;

/**
 * Description :
 * Created by yue on 2021/11/29
 */

public class AnimWrapper {

    private final List<FrameResource> resourceList;

    public AnimWrapper(List<FrameResource> resourceList) {
        this.resourceList = resourceList;
    }

    public List<FrameResource> getResourceList() {
        if (resourceList == null) {
            return new ArrayList<>();
        }
        return resourceList;
    }

    public boolean hasDuration() {
        for (FrameResource frameResource : getResourceList()) {
            if (frameResource.duration != 0) {
                return true;
            }
        }
        return false;
    }

    public void setDuration(int duration) {
        for (FrameResource frameResource : getResourceList()) {
            frameResource.duration = duration;
        }
    }

    public static class FrameResource {

        public static final int TYPE_LOCAL = 0;
        public static final int TYPE_DISK = 1;

        private int sourceType;
        private String resource;
        private long duration;

        public int getSourceType() {
            return sourceType;
        }

        public void setSourceType(int sourceType) {
            this.sourceType = sourceType;
        }

        public int getResourceId() {
            return Integer.parseInt(resource.replace("@", ""));
        }

        public long getDuration() {
            return duration;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }

        public String getResource() {
            return resource;
        }

        public void setResource(String resource) {
            this.resource = resource;
        }

        @Override
        public String toString() {
            return "AnimFrame{" +
                    ", resource='" + resource + '\'' +
                    ", duration=" + duration +
                    '}';
        }
    }
}
