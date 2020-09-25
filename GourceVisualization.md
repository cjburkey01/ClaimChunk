### Gource Source Visualization

The following is a little guide to generating a Gource source visualization for ClaimChunk. There isn't a good reason to do this other than it looks cool (this guide is more for my own personal reference).

Gource can be found [**here**](https://github.com/acaudwell/Gource).

#### Output the name of the commits as captions:
`git log --pretty='%at|%s' | sort -n > my_captions.txt`

#### Open and start the visualizer:
`gource -1920x1080 -e 0.05 --key --title "ClaimChunk" --seconds-per-day 3 --auto-skip-seconds 1 --file-idle-time 0 --max-file-lag 1 --caption-file my_captions.txt --caption-duration 2 --output-framerate 60 --output-ppm-stream source_visual.ppm`
> Note: You can press escape to exit the visualization.

The<br />
`--output-framerate 60 --output-ppm-stream source_visual.ppm`<br />
can be removed if you don't want to generate a video)

#### Use FFMPEG to convert to MP4:
`ffmpeg -y -r 60 -f image2pipe -vcodec ppm -i source_visual.ppm -vcodec libx264 -preset medium -pix_fmt yuv420p -crf 1 -threads 0 -bf 0 source_visual.mp4`

#### Finally, delete temporary files (`del` instead of `rm` on Windows):
`rm my_captions.txt` & `rm source_visual.ppm`
