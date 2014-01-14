import re

data = """
}else if(filename.endsWith(".aac")){
}else if(filename.endsWith(".ai")){
}else if(filename.endsWith(".aiff")){
}else if(filename.endsWith(".avi")){
}else if(filename.endsWith(".bmp")){
}else if(filename.endsWith(".c")){
}else if(filename.endsWith(".cpp")){
}else if(filename.endsWith(".css")){
}else if(filename.endsWith(".dat")){
}else if(filename.endsWith(".dmg")){
}else if(filename.endsWith(".doc")){
}else if(filename.endsWith(".dotx")){
}else if(filename.endsWith(".dwg")){
}else if(filename.endsWith(".dxf")){
}else if(filename.endsWith(".eps")){
}else if(filename.endsWith(".exe")){
}else if(filename.endsWith(".flv")){
}else if(filename.endsWith(".gif")){
}else if(filename.endsWith(".h")){
}else if(filename.endsWith(".hpp")){
}else if(filename.endsWith(".html")){
}else if(filename.endsWith(".ics")){
}else if(filename.endsWith(".iso")){
}else if(filename.endsWith(".java")){
}else if(filename.endsWith(".jpg")){
}else if(filename.endsWith(".key")){
}else if(filename.endsWith(".mid")){
}else if(filename.endsWith(".mp3")){
}else if(filename.endsWith(".mp4")){
}else if(filename.endsWith(".mpg")){
}else if(filename.endsWith(".odf")){
}else if(filename.endsWith(".ods")){
}else if(filename.endsWith(".odt")){
}else if(filename.endsWith(".otp")){
}else if(filename.endsWith(".ots")){
}else if(filename.endsWith(".ott")){
}else if(filename.endsWith(".pdf")){
}else if(filename.endsWith(".php")){
}else if(filename.endsWith(".png")){
}else if(filename.endsWith(".ppt")){
}else if(filename.endsWith(".psd")){
}else if(filename.endsWith(".py")){
}else if(filename.endsWith(".qt")){
}else if(filename.endsWith(".rar")){
}else if(filename.endsWith(".rb")){
}else if(filename.endsWith(".rtf")){
}else if(filename.endsWith(".sql")){
}else if(filename.endsWith(".tga")){
}else if(filename.endsWith(".tgz")){
}else if(filename.endsWith(".tiff")){
}else if(filename.endsWith(".txt")){
}else if(filename.endsWith(".wav")){
}else if(filename.endsWith(".xls")){
}else if(filename.endsWith(".xlsx")){
}else if(filename.endsWith(".xml")){
}else if(filename.endsWith(".yml")){
}else if(filename.endsWith(".zip")){
}else if(filename.endsWith(".asc")){
}else if(filename.endsWith(".asm")){
}else if(filename.endsWith(".c")){
}else if(filename.endsWith(".cer")){
}else if(filename.endsWith(".cfg")){
}else if(filename.endsWith(".cpp")){
}else if(filename.endsWith(".css")){
}else if(filename.endsWith(".h")){
}else if(filename.endsWith(".htm")){
}else if(filename.endsWith(".html")){
}else if(filename.endsWith(".java")){
}else if(filename.endsWith(".js")){
}else if(filename.endsWith(".json")){
}else if(filename.endsWith(".lua")){
}else if(filename.endsWith(".lua")){
}else if(filename.endsWith(".md")){
}else if(filename.endsWith(".mm")){
}else if(filename.endsWith(".mm")){
}else if(filename.endsWith(".pem")){
}else if(filename.endsWith(".pgp")){
}else if(filename.endsWith(".pl")){
}else if(filename.endsWith(".pub")){
}else if(filename.endsWith(".rb")){
}else if(filename.endsWith(".s")){
}else if(filename.endsWith(".sh")){
}else if(filename.endsWith(".sql")){
}else if(filename.endsWith(".xml")){

"""

names = """
aac.png
ai.png
aiff.png
avi.png
bmp.png
c.png
cpp.png
css.png
dat.png
dmg.png
doc.png
dotx.png
dwg.png
dxf.png
eps.png
exe.png
flv.png
gif.png
h.png
hpp.png
html.png
ics.png
iso.png
java.png
jpg.png
key.png
mid.png
mp3.png
mp4.png
mpg.png
odf.png
ods.png
odt.png
otp.png
ots.png
ott.png
pdf.png
php.png
png.png
ppt.png
psd.png
py.png
qt.png
rar.png
rb.png
rtf.png
sql.png
tga.png
tgz.png
tiff.png
txt.png
wav.png
xls.png
xlsx.png
xml.png
yml.png
zip.png
"""

extensions = sorted(set(data.split("\n")))

out = []

for item in names.split():
    #print item.split(".")[0]
    out.append("."+item.split(".")[0])


for i in extensions:
    a = i.split('"')
    try:
        if a[1] in out:
            print i,
            print """
                sItem = new DrawableItem(filename, R.drawable%s);
                sItem.setTag(0, "");
                sItem.setTag(1, fullfn);
                adapter.add(sItem);""" % (a[1])
        else:
            print i
    except IndexError:
        pass

