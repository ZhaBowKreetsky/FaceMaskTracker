
package face.facetracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import face.facetracker.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.face.Face;

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
class FaceGraphic extends GraphicOverlay.Graphic {
    private static final float FACE_POSITION_RADIUS = 10.0f;
    private static final float ID_TEXT_SIZE = 40.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

    // Factor that controls the size of overlay
    private static final float HEAD_SIZE_FACTOR = 2.0f;
    // Thresholds to decide whether an eye is open, half open or closed
    private static final float THRESHOLD_EYES_OPEN = 0.7f;
    private static final float THRESHOLD_EYES_HALF_OPEN = 0.4f;
    // Thresholds to decide whether a mouth is open, half open or closed
    private static final float THRESHOLD_MOUTH_OPEN = 0.6f;
    private static final float THRESHOLD_MOUTH_HALF_OPEN = 0.2f;
    // Mininum degree to tile the head
    private static final float DIFF_ROTATE_DEGREE = 2.0f;

    private float mLastDegree = 0f;

    private Paint mFacePositionPaint;
    private Paint mIdPaint;
    private Paint mBoxPaint;

    private volatile Face mFace;
    private int mFaceId;

    private Context mContext;
    private boolean mRotateEnabled = true;

    FaceGraphic(GraphicOverlay overlay, Context context) {
        super(overlay);

        mContext = context;

        final int selectedColor = Color.RED;

        mFacePositionPaint = new Paint();
        mFacePositionPaint.setColor(selectedColor);

        mIdPaint = new Paint();
        mIdPaint.setColor(selectedColor);
        mIdPaint.setTextSize(ID_TEXT_SIZE);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(selectedColor);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
    }

    void setId(int id) {
        mFaceId = id;
    }

    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    void updateFace(Face face) {
        mFace = face;
        postInvalidate();
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;
        if (face == null) {
            return;
        }

        // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(face.getPosition().x + face.getWidth() / 2);
        float y = translateY(face.getPosition().y + face.getHeight() / 2);

        if (mRotateEnabled) {
            float degree = face.getEulerZ();

            // Rotate canvas only when diff is larger than a threshold to avoid vibration
            if (Math.abs(degree - mLastDegree) > DIFF_ROTATE_DEGREE) {
                mLastDegree = degree;
            }
        }

        if (mLastDegree != 0) {
            canvas.save();
            canvas.rotate(mLastDegree, x, y);
        }
//        canvas.drawCircle(x, y, FACE_POSITION_RADIUS, mFacePositionPaint);
//        canvas.drawText("id: " + mFaceId, x + ID_X_OFFSET, y + ID_Y_OFFSET, mIdPaint);

        // Draws a bounding box around the face.
        float xOffset = scaleX(face.getWidth() / 3.0f) * HEAD_SIZE_FACTOR;
        float yOffset = scaleY(face.getHeight() / 3.0f) * HEAD_SIZE_FACTOR;
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
//        canvas.drawRect(left, top, right, bottom, mBoxPaint);

        // Draw from resource
        Drawable ears = ContextCompat.getDrawable(mContext, R.drawable.ears_small);
        ears.setBounds((int)left, (int)(top), (int)right, (int)(bottom - yOffset));
        ears.draw(canvas);

        if (face.getIsLeftEyeOpenProbability() > THRESHOLD_EYES_OPEN || face.getIsLeftEyeOpenProbability() == -1.0) {
            Drawable leftEye = ContextCompat.getDrawable(mContext, R.drawable.female_003_left_eye01);
            leftEye.setBounds((int) left, (int) (top + 35), (int) right, (int) bottom);
            leftEye.draw(canvas);
        } else if (face.getIsLeftEyeOpenProbability() < THRESHOLD_EYES_HALF_OPEN) {
            Drawable leftEye = ContextCompat.getDrawable(mContext, R.drawable.female_003_left_eye02);
            leftEye.setBounds((int) left, (int) (top + 35), (int) right, (int) bottom);
            leftEye.draw(canvas);
        } else {
            Drawable leftEye = ContextCompat.getDrawable(mContext, R.drawable.female_003_left_eye03);
            leftEye.setBounds((int) left, (int) (top + 35), (int) right, (int) bottom);
            leftEye.draw(canvas);
        }

        if (face.getIsRightEyeOpenProbability() > THRESHOLD_EYES_OPEN || face.getIsRightEyeOpenProbability() == -1.0) {
            Drawable rightEye = ContextCompat.getDrawable(mContext, R.drawable.female_003_right_eye01);
            rightEye.setBounds((int) left, (int) (top + 35), (int) right, (int) bottom);
            rightEye.draw(canvas);
        } else if (face.getIsRightEyeOpenProbability() < THRESHOLD_EYES_HALF_OPEN) {
            Drawable rightEye = ContextCompat.getDrawable(mContext, R.drawable.female_003_right_eye02);
            rightEye.setBounds((int) left, (int) (top + 35), (int) right, (int) bottom);
            rightEye.draw(canvas);
        } else {
            Drawable rightEye = ContextCompat.getDrawable(mContext, R.drawable.female_003_right_eye03);
            rightEye.setBounds((int) left, (int) (top + 35), (int) right, (int) bottom);
            rightEye.draw(canvas);
        }


        if (face.getIsSmilingProbability() > THRESHOLD_MOUTH_OPEN) {
            Drawable mouth = ContextCompat.getDrawable(mContext, R.drawable.female_003_smile03);
            mouth.setBounds((int) left, (int) (top + 90), (int) right, (int) bottom);
            mouth.draw(canvas);
        } else if (face.getIsSmilingProbability() > THRESHOLD_MOUTH_HALF_OPEN) {
            Drawable mouth = ContextCompat.getDrawable(mContext, R.drawable.female_003_smile02);
            mouth.setBounds((int) left, (int) (top + 90), (int) right, (int) bottom);
            mouth.draw(canvas);
        } else {
            Drawable mouth = ContextCompat.getDrawable(mContext, R.drawable.female_003_smile01);
            mouth.setBounds((int) left, (int) (top + 90), (int) right, (int) bottom);
            mouth.draw(canvas);
        }

        if (mLastDegree != 0) {
            canvas.restore();
        }
    }
}
