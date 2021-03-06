/*
 * This code is derived from WOBAFormat.java, a part of Kreative
 * Software's PowerPaint application. The original source code can
 * be found on GitHub, here: https://github.com/kreativekorp/powerpaint/blob/master/main/java/PowerPaint/src/com/kreative/paint/format/WOBAFormat.java
 * <p>
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * <a href="http://www.mozilla.org/MPL/">http://www.mozilla.org/MPL/</a>
 * <p>
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 * <p>
 * Alternatively, the contents of this file may be used under the terms
 * of the GNU Lesser General Public License (the "LGPL License"), in which
 * case the provisions of LGPL License are applicable instead of those
 * above. If you wish to allow use of your version of this file only
 * under the terms of the LGPL License and not to allow others to use
 * your version of this file under the MPL, indicate your decision by
 * deleting the provisions above and replace them with the notice and
 * other provisions required by the LGPL License. If you do not delete
 * the provisions above, a recipient may use your version of this file
 * under either the MPL or the LGPL License.
 *
 * @author Rebecca G. Bettencourt, Kreative Software
 * @author Matt DeFano
 */

package com.defano.wyldcard.stackreader.decoder;

import java.awt.*;
import java.awt.image.BufferedImage;

public interface WOBAImageDecoder {

    @SuppressWarnings("PointlessArithmeticExpression")
    default BufferedImage decodeImage(Rectangle boundRect, Rectangle maskRect, Rectangle imageRect, int imageSize, byte[] imageData, int maskSize, byte[] maskData) {
        BufferedImage image;

        try {
            byte[] maskBytes = decodeWOBA(boundRect, maskRect, maskData, 0, maskSize);
            byte[] imageBytes = decodeWOBA(boundRect, imageRect, imageData, 0, imageSize);
            int[] pixels = new int[imageBytes.length * 8];
            for (int ii = 0, mi = 0, pi = 0; ii < imageBytes.length && mi < maskBytes.length && pi < pixels.length; ii++, mi++, pi += 8) {
                byte ibt = imageBytes[ii];
                byte mbt = maskBytes[mi];
                pixels[pi + 0] = ((ibt & 0x80) > 0) ? 0xFF000000 : ((mbt & 0x80) > 0) ? 0xFFFFFFFF : 0;
                pixels[pi + 1] = ((ibt & 0x40) > 0) ? 0xFF000000 : ((mbt & 0x40) > 0) ? 0xFFFFFFFF : 0;
                pixels[pi + 2] = ((ibt & 0x20) > 0) ? 0xFF000000 : ((mbt & 0x20) > 0) ? 0xFFFFFFFF : 0;
                pixels[pi + 3] = ((ibt & 0x10) > 0) ? 0xFF000000 : ((mbt & 0x10) > 0) ? 0xFFFFFFFF : 0;
                pixels[pi + 4] = ((ibt & 0x08) > 0) ? 0xFF000000 : ((mbt & 0x08) > 0) ? 0xFFFFFFFF : 0;
                pixels[pi + 5] = ((ibt & 0x04) > 0) ? 0xFF000000 : ((mbt & 0x04) > 0) ? 0xFFFFFFFF : 0;
                pixels[pi + 6] = ((ibt & 0x02) > 0) ? 0xFF000000 : ((mbt & 0x02) > 0) ? 0xFFFFFFFF : 0;
                pixels[pi + 7] = ((ibt & 0x01) > 0) ? 0xFF000000 : ((mbt & 0x01) > 0) ? 0xFFFFFFFF : 0;
            }

            image = new BufferedImage(boundRect.width, boundRect.height, BufferedImage.TYPE_INT_ARGB);
            image.setRGB(0, 0, boundRect.width, boundRect.height, pixels, 0, snap32(boundRect).width);
            return image;

        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    default byte[] decodeWOBA(Rectangle totr, Rectangle r, byte[] data, int offset, int l) throws ArrayIndexOutOfBoundsException {
        Rectangle tr = snap32(totr);
        int trw = tr.width >> 3;
        Rectangle rf = snap32(r);
        int rw = rf.width >> 3;
        byte[] stuff = new byte[trw * tr.height];
        if (l == 0) {
            if (r.width > 0 && r.height > 0) {
                int sbyte = r.x >> 3;
                int sbit = r.x & 0x7;
                int ebyte = (r.x + r.width) >> 3;
                int ebit = (r.x + r.width) & 0x7;
                int base = trw * r.y;
                for (int y = r.y; y < r.y + r.height; y++) {
                    stuff[base + sbyte] = (byte) (0xFF >> sbit);
                    for (int x = sbyte + 1; x < ebyte; x++) {
                        stuff[base + x] = (byte) 0xFF;
                    }
                    if (ebit > 0) stuff[base + ebyte] = (byte) (0xFF << (8 - ebit));
                    base += trw;
                }
            }
        } else {
            int p = offset;
            int y = rf.y - tr.y;
            int base = trw * y + ((rf.x - tr.x) >> 3);
            int pp = base;
            int repeat = 1;
            int dh = 0, dv = 0;
            byte[] patt = new byte[]{
                    (byte) 0xAA, (byte) 0x55, (byte) 0xAA, (byte) 0x55,
                    (byte) 0xAA, (byte) 0x55, (byte) 0xAA, (byte) 0x55
            };
            while (y < rf.y - tr.y + rf.height && p < data.length) {
                byte opcode = data[p++];
                if ((opcode & 0x80) == 0) {
                    int d = (opcode & 0x70) >> 4;
                    int z = opcode & 0x0F;
                    byte[] dat = new byte[d];
                    for (int i = 0; i < d; i++) dat[i] = data[p++];
                    while ((repeat--) > 0) {
                        pp += z;
                        for (int i = 0; i < d; i++) stuff[pp++] = dat[i];
                    }
                } else if ((opcode & 0xE0) == 0xA0) {
                    repeat = (opcode & 0x1F);
                    continue;
                } else if ((opcode & 0xE0) == 0xC0) {
                    int d = (opcode & 0x1F) << 3;
                    byte[] dat = new byte[d];
                    for (int i = 0; i < d; i++) dat[i] = data[p++];
                    while ((repeat--) > 0) {
                        for (int i = 0; i < d; i++) stuff[pp++] = dat[i];
                    }
                } else if ((opcode & 0xE0) == 0xE0) {
                    pp += ((opcode & 0x1F) << 4) * repeat;
                } else {
                    switch (opcode) {
                        case (byte) 0x80: {
                            byte[] dat = new byte[rw];
                            for (int i = 0; i < rw; i++) dat[i] = data[p++];
                            while ((repeat--) > 0) {
                                for (int i = 0; i < rw; i++) stuff[pp++] = dat[i];
                                y++;
                                base += trw;
                                pp = base;
                            }
                            repeat = 1;
                        }
                        break;
                        case (byte) 0x81: {
                            y += repeat;
                            base += trw * repeat;
                            pp = base;
                            repeat = 1;
                        }
                        break;
                        case (byte) 0x82: {
                            while ((repeat--) > 0) {
                                for (int i = 0; i < rw; i++) stuff[pp++] = -1;
                                y++;
                                base += trw;
                                pp = base;
                            }
                            repeat = 1;
                        }
                        break;
                        case (byte) 0x83: {
                            byte pb = data[p++];
                            while ((repeat--) > 0) {
                                patt[y & 0x7] = pb;
                                for (int i = 0; i < rw; i++) stuff[pp++] = pb;
                                y++;
                                base += trw;
                                pp = base;
                            }
                            repeat = 1;
                        }
                        break;
                        case (byte) 0x84: {
                            while ((repeat--) > 0) {
                                byte pb = patt[y & 0x7];
                                for (int i = 0; i < rw; i++) stuff[pp++] = pb;
                                y++;
                                base += trw;
                                pp = base;
                            }
                            repeat = 1;
                        }
                        break;
                        case (byte) 0x85: {
                            while ((repeat--) > 0) {
                                for (int i = 0; i < rw; i++) {
                                    stuff[pp] = stuff[pp - trw];
                                    pp++;
                                }
                                y++;
                                base += trw;
                                pp = base;
                            }
                            repeat = 1;
                        }
                        break;
                        case (byte) 0x86: {
                            while ((repeat--) > 0) {
                                for (int i = 0; i < rw; i++) {
                                    stuff[pp] = stuff[pp - (trw * 2)];
                                    pp++;
                                }
                                y++;
                                base += trw;
                                pp = base;
                            }
                            repeat = 1;
                        }
                        break;
                        case (byte) 0x87: {
                            while ((repeat--) > 0) {
                                for (int i = 0; i < rw; i++) {
                                    stuff[pp] = stuff[pp - (trw * 3)];
                                    pp++;
                                }
                                y++;
                                base += trw;
                                pp = base;
                            }
                            repeat = 1;
                        }
                        break;
                        case (byte) 0x88:
                            dh = 16;
                            dv = 0;
                            break;
                        case (byte) 0x89:
                            dh = 0;
                            dv = 0;
                            break;
                        case (byte) 0x8A:
                            dh = 0;
                            dv = 1;
                            break;
                        case (byte) 0x8B:
                            dh = 0;
                            dv = 2;
                            break;
                        case (byte) 0x8C:
                            dh = 1;
                            dv = 0;
                            break;
                        case (byte) 0x8D:
                            dh = 1;
                            dv = 1;
                            break;
                        case (byte) 0x8E:
                            dh = 2;
                            dv = 2;
                            break;
                        case (byte) 0x8F:
                            dh = 8;
                            dv = 0;
                            break;
                    }
                    continue;
                }

                repeat = 1;
                if (pp >= base + rw) {
                    if (dh != 0) {
                        byte[] row = new byte[rw];
                        for (int i = 0; i < rw; i++) row[i] = stuff[base + i];
                        int numshifts = (rw << 3) / dh;
                        while ((numshifts--) > 0) {
                            int acc = 0;
                            for (int i = 0; i < rw; i += 4) {
                                int tmp = ((row[i] & 0xFF) << 24) | ((row[i + 1] & 0xFF) << 16) | ((row[i + 2] & 0xFF) << 8) | (row[i + 3] & 0xFF);
                                int rowi = acc | (tmp >>> dh);
                                row[i] = (byte) ((rowi >>> 24) & 0xFF);
                                row[i + 1] = (byte) ((rowi >>> 16) & 0xFF);
                                row[i + 2] = (byte) ((rowi >>> 8) & 0xFF);
                                row[i + 3] = (byte) (rowi & 0xFF);
                                acc = tmp << (32 - dh);
                            }
                            for (int i = 0; i < rw; i++) stuff[base + i] ^= row[i];
                        }
                    }
                    if (dv != 0 && y - dv >= 0) {
                        for (int i = 0; i < rw; i++)
                            stuff[base + i] = (byte) (stuff[base + i] ^ stuff[(base - (trw * dv)) + i]);
                    }
                    y++;
                    base += trw;
                    pp = base;
                }
            }
        }

        return stuff;
    }

    default Rectangle snap32(Rectangle r) {
        int left = r.x & ~0x1F;
        int right = r.x + r.width;
        if ((right & 0x1F) != 0) {
            right |= 0x1F;
            right++;
        }
        return new Rectangle(left, r.y, right - left, r.height);
    }

}
