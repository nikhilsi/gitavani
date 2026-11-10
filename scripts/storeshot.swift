// storeshot <in.png> <out.png> <width> <height>   (build: swiftc -O scripts/storeshot.swift -o /tmp/storeshot)
// Scales a screenshot uniformly to cover width x height, crops the overflow evenly from the
// center, and writes an opaque PNG with no alpha channel (App Store screenshot rules).
import CoreGraphics
import Foundation
import ImageIO
import UniformTypeIdentifiers

let args = CommandLine.arguments
guard args.count == 5, let w = Int(args[3]), let h = Int(args[4]) else {
    fputs("usage: storeshot in.png out.png width height\n", stderr)
    exit(2)
}
guard let src = CGImageSourceCreateWithURL(URL(fileURLWithPath: args[1]) as CFURL, nil),
      let image = CGImageSourceCreateImageAtIndex(src, 0, nil) else {
    fputs("cannot read \(args[1])\n", stderr)
    exit(1)
}
let scale = max(Double(w) / Double(image.width), Double(h) / Double(image.height))
let drawW = Double(image.width) * scale
let drawH = Double(image.height) * scale
let rect = CGRect(x: (Double(w) - drawW) / 2, y: (Double(h) - drawH) / 2, width: drawW, height: drawH)

guard let ctx = CGContext(data: nil, width: w, height: h, bitsPerComponent: 8, bytesPerRow: 0,
                          space: CGColorSpace(name: CGColorSpace.sRGB)!,
                          bitmapInfo: CGImageAlphaInfo.noneSkipLast.rawValue) else { exit(1) }
ctx.interpolationQuality = .high
ctx.draw(image, in: rect)
guard let out = ctx.makeImage(),
      let dest = CGImageDestinationCreateWithURL(URL(fileURLWithPath: args[2]) as CFURL,
                                                 UTType.png.identifier as CFString, 1, nil) else { exit(1) }
CGImageDestinationAddImage(dest, out, nil)
guard CGImageDestinationFinalize(dest) else { exit(1) }
print("\(args[2]): \(w)x\(h), scale \(String(format: "%.4f", scale))")
