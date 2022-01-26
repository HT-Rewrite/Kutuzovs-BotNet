package me.kutuzov.client.w32;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.*;
import com.sun.jna.platform.win32.WinGDI.*;
import com.sun.jna.platform.win32.WinNT.*;
import com.sun.jna.ptr.ByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface GDI32 extends StdCallLibrary {
    GDI32 INSTANCE = Native.load("gdi32", GDI32.class, W32APIOptions.DEFAULT_OPTIONS);
    int SRCCOPY = 13369376;
    int NOTSRCCOPY = 3342344;

    HRGN ExtCreateRegion(Pointer var1, int var2, RGNDATA var3);
    int CombineRgn(HRGN var1, HRGN var2, HRGN var3, int var4);
    HRGN CreateRectRgn(int x1, int y1, int x2, int y2);
    HRGN CreateRoundRectRgn(int var1, int var2, int var3, int var4, int var5, int var6);
    HRGN CreatePolyPolygonRgn(POINT[] var1, int[] var2, int var3, int var4);
    boolean SetRectRgn(HRGN var1, int var2, int var3, int var4, int var5);
    int SetPixel(HDC var1, int var2, int var3, int var4);
    HDC CreateCompatibleDC(HDC var1);
    boolean DeleteDC(HDC var1);
    HBITMAP CreateDIBitmap(HDC var1, BITMAPINFOHEADER var2, int var3, Pointer var4, BITMAPINFO var5, int var6);
    HBITMAP CreateDIBSection(HDC var1, BITMAPINFO var2, int var3, PointerByReference var4, Pointer var5, int var6);
    HBITMAP CreateCompatibleBitmap(HDC var1, int var2, int var3);
    HANDLE SelectObject(HDC var1, HANDLE var2);
    boolean DeleteObject(HANDLE var1);
    int GetDeviceCaps(HDC var1, int var2);
    int GetDIBits(HDC var1, HBITMAP var2, int var3, int var4, Pointer var5, BITMAPINFO var6, int var7);
    int ChoosePixelFormat(HDC var1, ByReference var2);
    boolean SetPixelFormat(HDC var1, int var2, ByReference var3);
    int GetObject(HANDLE var1, int var2, Pointer var3);
    boolean BitBlt(HDC var1, int var2, int var3, int var4, int var5, HDC var6, int var7, int var8, int var9);
    boolean StretchBlt(HDC hdcDest, int xDest, int yDest, int wDest, int hDest, HDC hdcSrc, int xSrc, int ySrc, int wSrc, int hSrc, DWORD rop);
    boolean RoundRect(HDC hdc, int left, int top, int right, int bottom, int width, int height);
    int GetDCBrushColor(HDC hdc);
    int SetDCBrushColor(HDC hdc, int color);
    int GetDCPenColor(HDC hdc);
    int SetDCPenColor(HDC hdc, int color);
    boolean PaintRgn(HDC hdc, HRGN hrgn);
    boolean ExtTextOutA(HDC hdc, int x, int y, UINT options, RECT[] lprect, char[] lpString, UINT c, int[] lpDx);
    HBRUSH CreateSolidBrush(int color);
    int SetDIBits(HDC hdc, HBITMAP hbm, UINT start, UINT cLines, Pointer lpBits, BITMAPINFO lpbmi, UINT ColorUse);
    int SetDIBitsToDevice(HDC hdc, int xDest, int yDest, DWORD w, DWORD h, int xSrc, int ySrc, UINT StartScan, UINT cLines, Pointer lpvBits, BITMAPINFO lpbmi, UINT colorUse);
    int StretchDIBits(HDC hdc, int xDest, int yDest, int DestWidth, int DestHeight, int xSrc, int ySrc, int SrcWidth, int SrcHeight, Pointer lpBits, BITMAPINFO lpbmi, UINT iUsage, DWORD rop);
    boolean StrokeAndFillPath(HDC hdc);
    boolean TransparentBlt(HDC hdcDest, int xoriginDest, int yoriginDest, int wDest, int hDest, HDC hdcSrc, int xoriginSrc, int yoriginSrc, int wSrc, int hSrc, UINT crTransparent);
}