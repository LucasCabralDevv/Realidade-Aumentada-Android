package com.lucascabral.realidadeaumentadaandroid

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.SkeletonNode
import com.google.ar.sceneform.animation.ModelAnimator
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var arFragment: ArFragment
    private lateinit var model: Uri
    private var renderable: ModelRenderable? = null
    private var animator: ModelAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFragment = sceneformFragment as ArFragment
        model = Uri.parse("model_fight.sfb")

        arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->

            if (plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING) {

                return@setOnTapArPlaneListener
            }
            var anchor = hitResult.createAnchor()
            placeObject(arFragment, anchor, model)
        }

        animateKickButton.setOnClickListener { animateModel("Character|Kick") }
        animateIdleButton.setOnClickListener { animateModel("Character|Idle") }
        animateBoxingButton.setOnClickListener { animateModel("Character|Boxing") }
    }

    private fun animateModel(name: String) {

        animator?.let {
            if (it.isRunning) {
                it.end()
            }
        }

        renderable?.let { modelRenderable ->  
            val data = modelRenderable.getAnimationData(name)
            animator = ModelAnimator(data, modelRenderable)
            animator?.start()
        }
    }

    private fun placeObject(arFragment: ArFragment, anchor: Anchor?, model: Uri?) {

        ModelRenderable.builder()
            .setSource(arFragment.context, model)
            .build()
            .thenAccept { model ->
                renderable = model
                addToScene(arFragment, anchor, model)
            }

            .exceptionally {
                val builder = AlertDialog.Builder(this)
                builder.setMessage(it.message).setTitle("Error")
                val dialog = builder.create()
                dialog.show()
                return@exceptionally null
            }
    }

    private fun addToScene(arFragment: ArFragment, anchor: Anchor?, model: ModelRenderable?) {

        val anchorNode = AnchorNode(anchor)
        val skeletonNode = SkeletonNode()
        skeletonNode.renderable = renderable
        val node = TransformableNode(arFragment.transformationSystem)
        node.addChild(skeletonNode)
        node.setParent(anchorNode)

        arFragment.arSceneView.scene.addChild(anchorNode)
    }
}