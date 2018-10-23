# Blur-LIB-Android

A library for Blurring the background of a View.

[<img src="https://cdn.dribbble.com/users/655449/screenshots/2179342/menu_dribble.gif" width="500"/>](https://youtu.be/sYPqS0px61Q)

[See demo on youtube](https://youtu.be/sYPqS0px61Q)

## How it works
The blurring is really fast since everything is fully hardware accelerated. The background View that is to be blurred is rendered into a SurfaceTexture using Surface.lockHardwareCanvas(). The SurfaceTexture can be down sampled for better performance, at the cost of lower quality.
The SurfaceTexture is then blurred and rendered in a SurfaceView or TextureView using OpenGL.

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

#Blur radius
<table>
  <tr>
    <td width="50%"><div class="highlight"><pre>
 app:blurRadius="100.0" </pre></div></td>
    <td>
     blurBehindLayout.blurRadius = 100f
    </td>
  </tr>
</table>


## Contact

You can reach me on Twitter as [@zellah](https://twitter.com/zellah) or [email](mailto:hello@danielzeller.no).


## Who's behind this?

Developed by [@zellah](https://twitter.com/zellah) at [danielzeller.no](http://danielzeller.no/), a freelance developer situated in Oslo.

