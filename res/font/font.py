#!/usr/bin/env python
# -*- coding: iso-8859-15 -*-
# (c): Matti J. Katila


"""
Creates a font image file from truetype fonts with pygame.

Usage:
   ./font.py
   for i in `ls *.bmp`; do convert $i $i.png ; done
"""



from pygame import font, image, Surface, draw
import sys

symbols = ' §œ!"@#£€$%&/{([)]=}?\\+`\'^*<>|;,:._-'
nums    =  '0123456789'
alphabet= 'abcdefghijklmnopqrstuvwxyz'
Alphabet= 'ABCDEFGHIJKLMNOPQRSTUVWXYZ' 

oikkoset = 'åäöûüêëíì' + 'ÅÄÖÛÜÊËÍÌ' 

alphabet = nums + alphabet + Alphabet

nums = oikkoset + symbols #+ 'åûüêëíìÅÛÜÊËÍÌ'

font.init()

#for fontFile in sys.argv[1:]:
#    print fontFile

import math
#print len(chars), math.sqrt(len(chars))

COLS = 8


goals = [64, 128, 256] #, 512, 1024]


def findGoal(textureWidth, fontFile):
    """ Returns a font which can render in a given height and width.
    """
    retFont = None
    for sizeH in range(2, 1000):
        f = font.Font(fontFile, sizeH)

        maxHeight = f.size(chars)[1]


        maxWidth = 0
        for ch in chars:
            maxWidth = max(f.size(ch)[0], maxWidth)

        maxWidth *= COLS
        maxHeight *= COLS

        if textureWidth < max(maxHeight, maxWidth):
            retFont = f
            break

        retFont = f

    return retFont

def imagine(fontFile, bold, italic):
    print fontFile
    # find a size.....
    for sqTextSize in goals:
        f = font.Font(fontFile, sqTextSize/COLS)
        #f = findGoal(sqTextSize, fontFile)
        print 'h', f.get_height()


        #maxHeight = f.size(chars)[1]
        #maxWidth = 0
        #for ch in chars:
        #    maxWidth = max(f.size(ch)[0], maxWidth)
        #sq = max(maxWidth, maxHeight)

        info = []


        LIST = [alphabet, nums]
        print LIST
        for listInd in range(len(LIST)):
            list = LIST[listInd]

            x,y = 0, f.get_descent()
            print y
            surface = Surface((sqTextSize, sqTextSize))
            #draw.line(surface, (255,0,0), (0,0), (sqTextSize, 0))
            for ind in range(len(list)):
                ch = list[ind]
                s = f.render(ch, 1, (255,255,255,255))
                surface.blit(s, (x,y))

                info.append( (ch, x, y-f.get_descent(), f.size(ch)) )
                
                x += sqTextSize/COLS
                if ind % COLS == COLS - 1:
                    y+= sqTextSize/COLS
                    x = 0
                
            image.save(surface, fontFileShort+
                       '-bold='+str(bold)+
                       '-italic='+str(italic)+
                       '-nro='+str(listInd)+
                       '-descent='+str(-f.get_descent()) +
                       '-size='+str(sqTextSize)+'x'+str(sqTextSize)+".bmp")
    
            #print info


DIR = '/usr/share/fonts/truetype/freefont/'

FONTS = {
    'mono':[ 'FreeMono.ttf', 'FreeMonoBold.ttf',
             'FreeMonoOblique.ttf', 'FreeMonoBoldOblique.ttf' ],
    'sans': ['FreeSans.ttf','FreeSansBold.ttf',
             'FreeSansOblique.ttf', 'FreeSansBoldOblique.ttf' ],
    'serif': ['FreeSerif.ttf','FreeSerifBold.ttf',
              'FreeSerifItalic.ttf','FreeSerifBoldItalic.ttf' ]
    }

for fontFileShort in FONTS.keys():
    print fontFileShort

    files = FONTS[fontFileShort]

    imagine(DIR+files[0], 0,0)
    imagine(DIR+files[1], 1,0)
    imagine(DIR+files[2], 0,1)
    imagine(DIR+files[3], 1,1)

#for fontFileShort in font.get_fonts():
#    print fontFileShort

#    if not fontFileShort.startswith('free'): continue

#    fontFile = font.match_font(fontFileShort, False, False)
#    imagine(fontFile, 0,0)
#    fontFile = font.match_font(fontFileShort,False, True)
#    imagine(fontFile, 0,1)
#    fontFile = font.match_font(fontFileShort,True, False)
#    imagine(fontFile, 1,0)
#    fontFile = font.match_font(fontFileShort,True, True)
#    imagine(fontFile, 1,1)
    
