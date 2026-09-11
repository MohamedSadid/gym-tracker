"""Align two exercise stills to one camera and write a two-frame animated PNG."""
import argparse
from pathlib import Path

import cv2
import numpy as np
from PIL import Image


def align_to(ref_bgr, src_bgr):
    h, w = ref_bgr.shape[:2]
    src = cv2.resize(src_bgr, (w, h))
    ref_g = cv2.cvtColor(ref_bgr, cv2.COLOR_BGR2GRAY)
    src_g = cv2.cvtColor(src, cv2.COLOR_BGR2GRAY)
    orb = cv2.ORB_create(4000)
    kp_ref, des_ref = orb.detectAndCompute(ref_g, None)
    kp_src, des_src = orb.detectAndCompute(src_g, None)
    if des_ref is None or des_src is None:
        return src
    matches = cv2.BFMatcher(cv2.NORM_HAMMING, crossCheck=True).match(des_ref, des_src)
    matches = sorted(matches, key=lambda m: m.distance)[:120]
    pts_ref = np.float32([kp_ref[m.queryIdx].pt for m in matches])
    pts_src = np.float32([kp_src[m.trainIdx].pt for m in matches])
    matrix, _ = cv2.estimateAffinePartial2D(
        pts_src, pts_ref, method=cv2.RANSAC, ransacReprojThreshold=3.0
    )
    if matrix is None:
        return src
    return cv2.warpAffine(
        src, matrix, (w, h), flags=cv2.INTER_LINEAR, borderMode=cv2.BORDER_REPLICATE
    )


def crop(im, margin=24):
    h, w = im.shape[:2]
    return im[margin : h - margin, margin : w - margin]


def to_pil(bgr):
    return Image.fromarray(cv2.cvtColor(bgr, cv2.COLOR_BGR2RGB))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--lockout", required=True)
    parser.add_argument("--bottom", required=True)
    parser.add_argument("--out", required=True)
    args = parser.parse_args()

    lockout = cv2.imread(args.lockout)
    bottom = align_to(lockout, cv2.imread(args.bottom))
    lockout, bottom = crop(lockout), crop(bottom)
    frames = [to_pil(lockout), to_pil(bottom)]
    out = Path(args.out)
    out.parent.mkdir(parents=True, exist_ok=True)
    frames[0].save(
        out,
        format="PNG",
        save_all=True,
        append_images=frames[1:],
        duration=[700, 700],
        loop=0,
        disposal=2,
    )
    print(out)


if __name__ == "__main__":
    main()
