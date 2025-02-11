package me.kutuzov.client.w32;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.WinDef.*;
import com.sun.jna.platform.win32.WinGDI;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface User32 extends StdCallLibrary, WinUser, WinNT {
    User32 INSTANCE = Native.load("user32", User32.class, W32APIOptions.DEFAULT_OPTIONS);
    int IDI_ERROR = 32513;
    int IDI_WARNING = 32515;
    int IDI_HAND = 32513;
    int IDI_INFORMATION = 32516;
    int WH_CBT = 5;
    int MB_SYSTEMMODAL = 0x00001000;
    int MB_OK = 0x00000000;
    int MB_OKCANCEL = 0x00000001;
    int MB_ICONWARNING = 0x00000030;
    int MB_ICONERROR = 0x00000010;
    int MB_ICONINFORMATION = 0x00000040;
    int MB_ICONSTOP = MB_ICONERROR;

    boolean DrawIcon(HDC hDC, int X, int Y, int hIcon);
    int LoadIconA(HINSTANCE hInstance, int icon);
    int MessageBoxW(HWND hWnd, char[] lpText, char[] lpCaption, UINT uType);
    HDC GetWindowDC(HWND hwnd);
    int FillRect(HDC hDC, RECT lprc, HBRUSH hbr);
    int FrameRect(HDC hDC, RECT lprc, HBRUSH hbr);

    /* PreDefined */
    HWND HWND_MESSAGE = new HWND(Pointer.createConstant(-3));
    int CS_GLOBALCLASS = 16384;
    int WS_EX_TOPMOST = 8;
    int DEVICE_NOTIFY_WINDOW_HANDLE = 0;
    int DEVICE_NOTIFY_SERVICE_HANDLE = 1;
    int DEVICE_NOTIFY_ALL_INTERFACE_CLASSES = 4;
    int SW_SHOWDEFAULT = 10;
    HDC GetDC(HWND var1);
    int ReleaseDC(HWND var1, HDC var2);
    HWND FindWindow(String var1, String var2);
    int GetClassName(HWND var1, char[] var2, int var3);
    boolean GetGUIThreadInfo(int var1, WinUser.GUITHREADINFO var2);
    boolean GetWindowInfo(HWND var1, WinUser.WINDOWINFO var2);
    boolean GetWindowRect(HWND var1, RECT var2);
    boolean GetClientRect(HWND var1, RECT var2);
    int GetWindowText(HWND var1, char[] var2, int var3);
    int GetWindowTextLength(HWND var1);
    int GetWindowModuleFileName(HWND var1, char[] var2, int var3);
    int GetWindowThreadProcessId(HWND var1, IntByReference var2);
    boolean EnumWindows(WinUser.WNDENUMPROC var1, Pointer var2);
    boolean EnumChildWindows(HWND var1, WinUser.WNDENUMPROC var2, Pointer var3);
    boolean EnumThreadWindows(int var1, WinUser.WNDENUMPROC var2, Pointer var3);
    boolean BringWindowToTop(HWND var1);
    boolean FlashWindowEx(WinUser.FLASHWINFO var1);
    HICON LoadIcon(HINSTANCE var1, String var2);
    WinNT.HANDLE LoadImage(HINSTANCE var1, String var2, int var3, int var4, int var5, int var6);
    boolean DestroyIcon(HICON var1);
    int GetWindowLong(HWND var1, int var2);
    int SetWindowLong(HWND var1, int var2, int var3);
    BaseTSD.LONG_PTR GetWindowLongPtr(HWND var1, int var2);
    Pointer SetWindowLongPtr(HWND var1, int var2, Pointer var3);
    boolean SetLayeredWindowAttributes(HWND var1, int var2, byte var3, int var4);
    boolean GetLayeredWindowAttributes(HWND var1, IntByReference var2, ByteByReference var3, IntByReference var4);
    boolean UpdateLayeredWindow(HWND var1, HDC var2, POINT var3, WinUser.SIZE var4, HDC var5, POINT var6, int var7, WinUser.BLENDFUNCTION var8, int var9);
    int SetWindowRgn(HWND var1, HRGN var2, boolean var3);
    boolean GetKeyboardState(byte[] var1);
    short GetAsyncKeyState(int var1);
    WinUser.HHOOK SetWindowsHookEx(int var1, WinUser.HOOKPROC var2, HINSTANCE var3, int var4);
    LRESULT CallNextHookEx(WinUser.HHOOK var1, int var2, WPARAM var3, LPARAM var4);
    boolean UnhookWindowsHookEx(WinUser.HHOOK var1);
    int GetMessage(WinUser.MSG var1, HWND var2, int var3, int var4);
    boolean PeekMessage(WinUser.MSG var1, HWND var2, int var3, int var4, int var5);
    boolean TranslateMessage(WinUser.MSG var1);
    LRESULT DispatchMessage(WinUser.MSG var1);
    void PostMessage(HWND var1, int var2, WPARAM var3, LPARAM var4);
    int PostThreadMessage(int var1, int var2, WPARAM var3, LPARAM var4);
    void PostQuitMessage(int var1);
    int GetSystemMetrics(int var1);
    HWND SetParent(HWND var1, HWND var2);
    boolean IsWindowVisible(HWND var1);
    boolean MoveWindow(HWND var1, int var2, int var3, int var4, int var5, boolean var6);
    boolean SetWindowPos(HWND var1, HWND var2, int var3, int var4, int var5, int var6, int var7);
    boolean AttachThreadInput(DWORD var1, DWORD var2, boolean var3);
    boolean SetForegroundWindow(HWND var1);
    HWND GetForegroundWindow();
    HWND SetFocus(HWND var1);
    DWORD SendInput(DWORD var1, WinUser.INPUT[] var2, int var3);
    DWORD WaitForInputIdle(WinNT.HANDLE var1, DWORD var2);
    boolean InvalidateRect(HWND var1, RECT var2, boolean var3);
    boolean RedrawWindow(HWND var1, RECT var2, HRGN var3, DWORD var4);
    HWND GetWindow(HWND var1, DWORD var2);
    boolean UpdateWindow(HWND var1);
    boolean ShowWindow(HWND var1, int var2);
    boolean CloseWindow(HWND var1);
    boolean RegisterHotKey(HWND var1, int var2, int var3, int var4);
    boolean UnregisterHotKey(Pointer var1, int var2);
    boolean GetLastInputInfo(WinUser.LASTINPUTINFO var1);
    ATOM RegisterClassEx(WinUser.WNDCLASSEX var1);
    boolean UnregisterClass(String var1, HINSTANCE var2);
    HWND CreateWindowEx(int var1, String var2, String var3, int var4, int var5, int var6, int var7, int var8, HWND var9, HMENU var10, HINSTANCE var11, LPVOID var12);
    boolean DestroyWindow(HWND var1);
    boolean GetClassInfoEx(HINSTANCE var1, String var2, WinUser.WNDCLASSEX var3);
    LRESULT CallWindowProc(Pointer var1, HWND var2, int var3, WPARAM var4, LPARAM var5);
    LRESULT DefWindowProc(HWND var1, int var2, WPARAM var3, LPARAM var4);
    WinUser.HDEVNOTIFY RegisterDeviceNotification(WinNT.HANDLE var1, Structure var2, int var3);
    boolean UnregisterDeviceNotification(WinUser.HDEVNOTIFY var1);
    int RegisterWindowMessage(String var1);
    WinUser.HMONITOR MonitorFromPoint(POINT.ByValue var1, int var2);
    WinUser.HMONITOR MonitorFromRect(RECT var1, int var2);
    WinUser.HMONITOR MonitorFromWindow(HWND var1, int var2);
    BOOL GetMonitorInfo(WinUser.HMONITOR var1, WinUser.MONITORINFO var2);
    BOOL GetMonitorInfo(WinUser.HMONITOR var1, WinUser.MONITORINFOEX var2);
    BOOL EnumDisplayMonitors(HDC var1, RECT var2, WinUser.MONITORENUMPROC var3, LPARAM var4);
    BOOL GetWindowPlacement(HWND var1, WinUser.WINDOWPLACEMENT var2);
    BOOL SetWindowPlacement(HWND var1, WinUser.WINDOWPLACEMENT var2);
    BOOL AdjustWindowRect(RECT var1, DWORD var2, BOOL var3);
    BOOL AdjustWindowRectEx(RECT var1, DWORD var2, BOOL var3, DWORD var4);
    BOOL ExitWindowsEx(UINT var1, DWORD var2);
    BOOL LockWorkStation();
    boolean GetIconInfo(HICON var1, WinGDI.ICONINFO var2);
    LRESULT SendMessageTimeout(HWND var1, int var2, WPARAM var3, LPARAM var4, int var5, int var6, DWORDByReference var7);
    BaseTSD.ULONG_PTR GetClassLongPtr(HWND var1, int var2);
    int GetRawInputDeviceList(WinUser.RAWINPUTDEVICELIST[] var1, IntByReference var2, int var3);
    HWND GetDesktopWindow();
    boolean PrintWindow(HWND var1, HDC var2, int var3);
    boolean IsWindowEnabled(HWND var1);
    boolean IsWindow(HWND var1);
    HWND FindWindowEx(HWND var1, HWND var2, String var3, String var4);
    HWND GetAncestor(HWND var1, int var2);
    HWND GetParent(HWND var1);
    boolean GetCursorPos(POINT var1);
    boolean SetCursorPos(long var1, long var3);
    WinNT.HANDLE SetWinEventHook(int var1, int var2, HMODULE var3, WinUser.WinEventProc var4, int var5, int var6, int var7);
    boolean UnhookWinEvent(WinNT.HANDLE var1);
    HICON CopyIcon(HICON var1);
    int GetClassLong(HWND var1, int var2);
    int RegisterClipboardFormat(String var1);
    HWND GetActiveWindow();
    LRESULT SendMessage(HWND var1, int var2, WPARAM var3, LPARAM var4);
    int GetKeyboardLayoutList(int var1, HKL[] var2);
    HKL GetKeyboardLayout(int var1);
    boolean GetKeyboardLayoutName(char[] var1);
    short VkKeyScanExA(byte var1, HKL var2);
    short VkKeyScanExW(char var1, HKL var2);
    int MapVirtualKeyEx(int var1, int var2, HKL var3);
    int ToUnicodeEx(int var1, int var2, byte[] var3, char[] var4, int var5, int var6, HKL var7);
    int LoadString(HINSTANCE var1, int var2, Pointer var3, int var4);
    int ShowCursor(boolean bShow);
    HCURSOR GetCursor();
    boolean LockWindowUpdate(HWND hwnd);
}