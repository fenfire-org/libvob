#!/usr/bin/env python
# (c): Matti J. Katila


"""
Creates a font image file from truetype fonts with pygame.
:author: mkatila


"""



from pygame import font, image, Surface
import sys



chars = ' ßΩ!"@#£§$%&/{([)]=}?\\+`\'^*<>|;,:._-'+ \
        '0123456789'+ \
        'abcdefghijklmnopqrstuvxyzÂ‰ˆ˚¸ÍÎÌÏ'+ \
        'ABCDEFGHIJKLMNOPQRSTUVXYZ≈ƒ÷€‹ ÀÕÃ' 

font.init()

#for fontFile in sys.argv[1:]:
#    print fontFile

for fontFileShort in font.get_fonts():
    print fontFileShort

    if not fontFileShort.startswith('free'): continue
    
    fontFile = font.match_font(fontFileShort)
    
    f = font.Font(fontFile, 12)

    maxHeight = f.size(chars)[1]

    print maxHeight

    width = 0
    
    for ch in chars:
        width += f.size(ch)[0]
        
    print width, maxHeight

    surface = Surface((width, maxHeight))
    print surface

    x,y = 0, 0
    for ch in chars:
        s = f.render(ch, 1, (255,255,255,255))
        surface.blit(s, (x,y))
        x += f.size(ch)[0]
        

    image.save(surface, fontFileShort+".bmp")
    

