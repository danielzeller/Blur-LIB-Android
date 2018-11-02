# Blur-LIB-Android

A library for Blurring the background of a View.

[<img src="/Artwork/PhoneAndScreen.gif" width="400"/>](https://youtu.be/sYPqS0px61Q)

[See demo on youtube](https://youtu.be/sYPqS0px61Q)

## How it works
The blurring is really fast since everything is fully hardware accelerated. The background View that is to be blurred is rendered into a SurfaceTexture using Surface.lockHardwareCanvas().
The SurfaceTexture is then blurred using a Guassian blur algorithm and rendered in a SurfaceView or TextureView using OpenGL.


## Download
Via Gradle

```
   implementation 'no.danielzeller.blurbehindlib:blurbehindlib:1.0.0'
```
or Maven
```
   <dependency>
     <groupId>no.danielzeller.blurbehindlib</groupId>
     <artifactId>blurbehindlib</artifactId>
     <version>1.0.0</version>
     <type>pom</type>
   </dependency>
```

## Basics
Create a BlurBehindLayout from XML

```xml
    <no.danielzeller.blurbehindlib.BlurBehindLayout
        android:id="@+id/blurBehindLayout"
        android:layout_width="match_parent"
        android:layout_height="50dp"
       
        app:blurRadius="100.0" 
        app:updateMode="onScroll"
        app:useTextureView="false"
        app:blurPaddingVertical="50dp"
        app:blurTextureScale="0.5"
        app:useChildAlphaAsMask="false">
        <!-- Add Children -->
        </no.danielzeller.blurbehindlib.BlurBehindLayout>
```

Then you need to setup the View that is behind the BlurBehindLayout (the one that will be blurred).

```kotlin
     blurBehindLayout.viewBehind = viewToBlur
```

[<img src="/Artwork/Transition.gif" width="400"/>](https://youtu.be/sYPqS0px61Q)

#### Blur radius
<table>
  <tr>
    <td width="50%"><div class="highlight"><pre>app:blurRadius = "100.0"</pre></div></td>
    <td width="50%"><div class="highlight"><pre>blurBehindLayout.blurRadius = 100.f</pre></div></td>
  </tr>
</table>
Determines how strong the blur is. Should be a float value between 0f-200f. Default is 40f.


#### Update mode
<table>
  <tr>
    <td width="50%"><div class="highlight"><pre>app:updateMode = "continuously"</pre></div></td>
    <td width="50%"><div class="highlight"><pre>blurBehindLayout.updateMode = UpdateMode.CONTINUOUSLY</pre></div></td>
  </tr>
</table>
Determines how the BlurBehindLayout is updated. When updateMode is UpdateMode.CONTINUOUSLY, the renderer is called repeatedly to re-render the scene. 
When updateMode is UpdateMode.ON_SCROLL, the renderer only renders when a View is Scrolled.
When updateMode is UpdateMode.MANUALLY, the renderer only renders when the surface is created and when updateForMilliSeconds(..) is called manually. This is useful when animating the background or during a transition. 


#### Use TextureView
```
    app:useTextureView = "true"
```
This can only be changed in the constructor of the BlurBehindLayout either from xml or using the regular constructor from code. Default value is false. Using TextureView should only be used when SurfaceView is'nt an option, either because the Z-ordering breaks or if you animate the BlurBehindLayout's alpha value. Using TextureView instead of SurfaceView has a small impact on performance.


#### Blur texture scale
```
    app:blurTextureScale = "0.5"
```
Should be a value between 0.1f-1f. It's recommended to downsample at least to 0.5f. The scale has a big impact on performance, so try keeping it as low as possible. Default is 0.4f. This can only be set in the constructor of the BlurBehindLayout either from xml or using the regular constructor from code. 


#### Padding vertical 
```
    app:blurPaddingVertical = "50dp"
```
You can use this to make the Blur Texture larger than the BlurBehindLayout in the vertical direction. For instance when the background View is scrolled up and down it looks better with a padding, because it reduces flicker when new pixels enter the blurred area. 


#### Use child as alpha mask
```
    app:useChildAlphaAsMask = "true"
```
When this is true the first child View of the BlurBehindLayout is rendered into a texture. The alpha value of that texture is then used as mask for the Blur texture. When useChildAlphaAsMask is true, useTextureView will be forced to true as well in order to support transparency. 
This effect can be used for creating text with blurred background and so on. See the DialogFragment for an example. 



## Contact

You can reach me on Twitter as [@zellah](https://twitter.com/zellah) or [email](mailto:hello@danielzeller.no).


## Who's behind this?

Developed by Daniel Zeller - [danielzeller.no](http://danielzeller.no/), a freelance developer situated in Oslo, Norway.

