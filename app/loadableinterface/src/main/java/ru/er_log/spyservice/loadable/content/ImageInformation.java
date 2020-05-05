/*+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 + Copyright (C) 2020 Eldar Timraleev (aka CRaFT4ik). All rights reserved.                        +
 + Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file      +
 + except in compliance with the License. You may obtain a copy of the License at                 +
 + http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in   +
 + writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT    +
 + WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the       +
 + specific language governing permissions and limitations under the License.                     +
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package ru.er_log.spyservice.loadable.content;

import java.util.Date;

public class ImageInformation
{
    /**
     * author CodeBoy722
     * <p>
     * Custom Class that holds information of a folder containing images
     * on the device external storage, used to populate our RecyclerView of
     * picture folders
     */
    public static class ImageFolder
    {
        private String path;
        private String FolderName;
        private int numberOfPics = 0;
        private String firstPic;

        public ImageFolder() {}

        public ImageFolder(String path, String folderName)
        {
            this.path = path;
            FolderName = folderName;
        }

        public String getPath()
        {
            return path;
        }

        public void setPath(String path)
        {
            this.path = path;
        }

        public String getFolderName()
        {
            return FolderName;
        }

        public void setFolderName(String folderName)
        {
            FolderName = folderName;
        }

        public int getNumberOfPics()
        {
            return numberOfPics;
        }

        public void setNumberOfPics(int numberOfPics)
        {
            this.numberOfPics = numberOfPics;
        }

        public void addPics()
        {
            this.numberOfPics++;
        }

        public String getFirstPic()
        {
            return firstPic;
        }

        public void setFirstPic(String firstPic)
        {
            this.firstPic = firstPic;
        }
    }

    /**
     * Author CodeBoy722
     * <p>
     * Custom class for holding data of images on the device external storage
     */
    public static class PictureFace
    {
        private String pictureName;
        private String picturePath;
        private String pictureSize;
        private Date dateModified;
        private String imageUri;
        private Boolean selected = false;

        public PictureFace() {}

        public PictureFace(String pictureName, String picturePath, String pictureSize, String imageUri)
        {
            this.pictureName = pictureName;
            this.picturePath = picturePath;
            this.pictureSize = pictureSize;
            this.imageUri = imageUri;
        }


        public String getPictureName()
        {
            return pictureName;
        }

        public void setPictureName(String pictureName)
        {
            this.pictureName = pictureName;
        }

        public String getPicturePath()
        {
            return picturePath;
        }

        public void setPicturePath(String picturePath)
        {
            this.picturePath = picturePath;
        }

        public String getPictureSize()
        {
            return pictureSize;
        }

        public void setPictureSize(String pictureSize)
        {
            this.pictureSize = pictureSize;
        }

        public String getImageUri()
        {
            return imageUri;
        }

        public void setImageUri(String imageUri)
        {
            this.imageUri = imageUri;
        }

        public Boolean getSelected()
        {
            return selected;
        }

        public void setSelected(Boolean selected)
        {
            this.selected = selected;
        }

        public Date getDateModified()
        {
            return this.dateModified;
        }

        public void setPictureDateModified(Date date)
        {
            this.dateModified = date;
        }
    }
}
